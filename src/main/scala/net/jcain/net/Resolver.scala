package net.jcain.net

import akka.actor.Actor

object Resolver {
  // questions
  sealed trait ResourceType
  case object A extends ResourceType
  case object AAAA extends ResourceType
  case object CNAME extends ResourceType
  case object MX extends ResourceType
  case object NS extends ResourceType
  case object SOA extends ResourceType
  case object TXT extends ResourceType
  final case class Resolve(name: String, rtype: ResourceType, fullRecursion: Boolean = false)

  // answers: RR types
  sealed trait Resource
  final case class MxRR(preference: Int, exchange: String) extends Resource
  sealed trait FullResource
  final case class MxHost(hostname: String, ipv4: List[Ip4Addr], ipv6: List[Ip6Addr])
  final case class MxPref(preference: Int, exchanges: List[MxHost]) extends FullResource

  // reply messages
  final case class Result(question: Resolve, answer: List[Resource])
  final case class FullResult(question: Resolve, answer: List[FullResource])
  final case class NotFound(question: Resolve)
}

class Resolver extends Actor {

  import Resolver._

  def receive = {
    case Resolve(name, rtype, fullRecursion) =>
  }

}
