package net.jcain.net

import akka.actor.{Actor, ActorRef}
import collection.JavaConversions._
import java.time.Instant
import javax.naming.Context
import javax.naming.directory.{Attribute, InitialDirContext}
import java.util.Properties
import scala.collection.immutable.HashMap
import scala.collection.mutable
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Resolver {

  val Default_Positive_TTL =  1 hour
  val Default_Negative_TTL = 10 minutes

  //
  // ResourceTypes: used in requests
  //

  sealed trait ResourceType {
    def parseResult(attr: Attribute): Set[_ <: Resource]
    def asString: String
    override def toString = asString
  }

  object A extends ResourceType {
    def parseResult(attr: Attribute) =
      attr.getAll.foldLeft(Set.empty[A_RR])((set, name) => set + new A_RR(Ip4Addr(name.asInstanceOf[String])))
    def asString = "A"
  }

  object AAAA extends ResourceType {
    def parseResult(attr: Attribute) =
      attr.getAll.foldLeft(Set.empty[AAAA_RR])((set, name) => set + new AAAA_RR(Ip6Addr(name.asInstanceOf[String])))
    def asString = "AAAA"
  }

  object MX extends ResourceType {
    def parseResult(attr: Attribute) = {
      val prefs = attr.getAll.foldLeft(HashMap.empty[Int, List[String]])((map, entry) => {
        val pieces = entry.asInstanceOf[String].split(" ")
        val pref = pieces(0).toInt
        if (map.contains(pref))
          map + ((pref, pieces(1) :: map(pref)))
        else
          map + ((pref, List(pieces(1))))
      })
      prefs.foldLeft(Set.empty[Resource])({
        case (mxrrs, (pref, hosts)) =>
          mxrrs + new MX_RR(pref, hosts.foldLeft(HashMap.empty[String, IpSet])((map, host) => map + ((host, new IpSet(Set(), Set())))))
      })
    }
    def asString = "MX"
  }

  //
  // Resources: used in responses
  //

  sealed trait Resource

  class A_RR(val address: Ip4Addr) extends Resource {
    override def equals(other: Any) = other match {
      case that: A_RR => address == that.address
      case _ => false
    }
    override def hashCode = address.hashCode
    override def toString = s"A_RR(address=$address)"
  }

  class AAAA_RR(val address: Ip6Addr) extends Resource {
    override def equals(other: Any) = other match {
      case that: AAAA_RR => address == that.address
      case _ => false
    }
    override def hashCode = address.hashCode
    override def toString = s"AAAA_RR(address=$address)"
  }

  class MX_RR(val preference: Int, var exchanges: Map[String, IpSet]) extends Resource {
    def hostnames = exchanges.keySet
    def addIps(ips: Set[_ <: Resource], domain: String): Boolean = {
      if (exchanges.contains(domain)) ips match {
        case ip4s if ips.find(x => true).get.isInstanceOf[A_RR] =>
          exchanges(domain).ipv4 ++= ip4s.foldLeft(Set.empty[Ip4Addr])((set, a) => set + a.asInstanceOf[A_RR].address)
        case ip6s =>
          exchanges(domain).ipv6 ++= ip6s.foldLeft(Set.empty[Ip6Addr])((set, a) => set + a.asInstanceOf[AAAA_RR].address)
      }
      false
    }
    override def equals(other: Any) = other match {
      case that: MX_RR => preference == that.preference && exchanges == that.exchanges
      case _ => false
    }
    override def hashCode = 41 * (41 + preference) + exchanges.hashCode
    override def toString = s"MX_RR(preference=$preference, exchanges=$exchanges)"
  }

  class IpSet(var ipv4: Set[Ip4Addr], var ipv6: Set[Ip6Addr]) {
    override def equals(other: Any) = other match {
      case that: IpSet => ipv4 == that.ipv4 && ipv6 == that.ipv6
      case _ => false
    }
    override def hashCode = 41 * (41 + ipv4.hashCode) + ipv6.hashCode
    override def toString = s"IpSet(ipv4=$ipv4, ipv6=$ipv6)"
  }

  // request messages
  final case class Resolve(name: String, rtype: ResourceType)
  protected final case class ResolveMore(name: String, rtype: ResourceType, domain: String)
  protected object ExpireCacheEntries

  // reply messages
  final case class Result(name: String, rtype: ResourceType, answer: Set[_ <: Resource])
  final case class NotFound(name: String, rtype: ResourceType)
}

class Resolver(
  positiveTtl: FiniteDuration = Resolver.Default_Positive_TTL,
  negativeTtl: FiniteDuration = Resolver.Default_Negative_TTL
) extends Actor {

  import Resolver._

  class CacheTime(val name: String, val rtype: ResourceType, val time: Instant = Instant.now) {
    def compare(that: CacheTime) =
      if (time.isBefore(that.time)) -1 else if (time.isAfter(that.time)) 1 else 0
    override def equals(other: Any) = other match {
      case that: CacheTime => name == that.name && rtype == that.rtype && time == that.time
      case _ => false
    }
    override def hashCode = 41 * (41 * (41 + name.hashCode) + rtype.hashCode) + time.hashCode
    override def toString = s"CacheTime($name, $rtype, $time)"
  }

  implicit object CacheTimeOrdering extends Ordering[CacheTime] {
    def compare(x: CacheTime, y: CacheTime) = x compare y
  }

  class PendingMx(var actors: Set[ActorRef], var lookups: Set[(String, ResourceType)], val answer: Set[MX_RR]) {
    def addIps(exchange: String, rtype: ResourceType, domain: String, result: Set[_ <: Resolver.Resource]): Boolean = {
      // add IPs to the matching IPSet
      val (ip4s, ip6s) = if (rtype == A)
        (Some(result.foldLeft(Set.empty[Ip4Addr])((set, rr) => set + rr.asInstanceOf[A_RR].address)), None)
      else
        (None, Some(result.foldLeft(Set.empty[Ip6Addr])((set, rr) => set + rr.asInstanceOf[AAAA_RR].address)))
      answer.foreach(mxrr => {
        if (mxrr.exchanges.contains(exchange)) {
          if (rtype == A)
            mxrr.exchanges(exchange).ipv4 ++= ip4s.get
          else
            mxrr.exchanges(exchange).ipv6 ++= ip6s.get
        }
      })
      // remove (exchange, rtype) pair from lookups and report if it's empty
      lookups -= ((exchange, rtype))
      lookups.isEmpty
    }
  }

  val cache = mutable.HashMap.empty[(String, ResourceType), Set[_ <: Resolver.Resource]]
  val positiveCacheTimes = mutable.PriorityQueue.empty[CacheTime]
  val negativeCacheTimes = mutable.PriorityQueue.empty[CacheTime]
  val pending = mutable.HashMap.empty[String, PendingMx]
  val env = new Properties
  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory")
  val ctx = new InitialDirContext(env)

  context.system.scheduler.schedule(1 minute, 1 minute, self, ExpireCacheEntries)

  def receive = {
    case Resolve(name, rtype) =>
      lookUp(name, rtype) match {
        case Some(result) =>
          if (rtype == MX) {
            // look up A and AAAA for all mx hosts
            if (pending.contains(name)) {
              // add to pending set
              pending(name).actors += sender()
            } else {
              // build list of lookups required
              val lookups = result.foldLeft(Set.empty[(String, ResourceType)])((set, mxrr) => {
                set ++ (for (host <- mxrr.asInstanceOf[MX_RR].exchanges.keySet; t <- Set(A, AAAA)) yield (host, t))
              })
              // send the lookup requests
              lookups.foreach({ case (host, rtyp) => self ! ResolveMore(host, rtyp, name) })
              // create pending set
              pending(name) = new PendingMx(Set(sender()), lookups, result.asInstanceOf[Set[MX_RR]])
            }
          } else {
            // no more lookups needed; send the result
            sender() ! Result(name, rtype, result)
          }
        case None => sender() ! Resolver.NotFound(name, rtype)
      }

    case rm @ ResolveMore(name, rtype, domain) =>
      lookUp(name, rtype) match {
        case Some(result) => processPendingMx(name, rtype, domain, result)
        case None => processPendingMx(name, rtype, domain, Set())
      }

    case ExpireCacheEntries => expireCacheEntries()
  }

  protected def lookUp(name: String, rtype: ResourceType): Option[Set[_ <: Resolver.Resource]] =
    if (cache.contains((name, rtype))) {
      Some(cache((name, rtype)))
    } else {
      try {
        ctx.getAttributes(name, Array(rtype.asString)).get(rtype.asString) match {
          case null =>
            addToCache(name, rtype, Set())
            None
          case attr =>
            val result = rtype.parseResult(attr)
            addToCache(name, rtype, result)
            Some(result)
        }
      } catch {
        case e: javax.naming.NameNotFoundException =>
          addToCache(name, rtype, Set())
          None
      }
    }

  protected def processPendingMx(name: String, rtype: ResourceType, domain: String, result: Set[_ <: Resource]) =
    if (pending.contains(domain)) {
      val pendingMx = pending(domain)
      if (pendingMx.addIps(name, rtype, domain, result)) {
        // all hostnames are resolved for both A and AAAA; notify requesters
        pendingMx.actors.foreach(actor => actor ! Result(domain, MX, pendingMx.answer))
        pending -= domain
      }
    }

  protected def addToCache(name: String, rtype: ResourceType, result: Set[_ <: Resource]) = {
    cache((name, rtype)) = result
    (if (result.isEmpty) negativeCacheTimes else positiveCacheTimes) += new CacheTime(name, rtype)
  }

  protected def expireCacheEntries(now: Instant = Instant.now) = {
    val positiveExpiry = now.minusSeconds(positiveTtl.toSeconds)
    val negativeExpiry = now.minusSeconds(negativeTtl.toSeconds)
    var head = positiveCacheTimes.head
    while (head.time.isBefore(positiveExpiry)) {
      cache -= ((head.name, head.rtype))
      positiveCacheTimes.dequeue()
      head = positiveCacheTimes.head
    }
    head = negativeCacheTimes.head
    while (head.time.isBefore(negativeExpiry)) {
      cache -= ((head.name, head.rtype))
      negativeCacheTimes.dequeue()
      head = negativeCacheTimes.head
    }
  }

}
