package net.jcain.net

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class ResolverTest extends TestKit(ActorSystem("ResolverTest"))
with WordSpecLike with BeforeAndAfterAll with Matchers
with ImplicitSender {

  import Resolver._

  val Jcain_v4_MX = Set(Ip4Addr("75.127.96.41"))
  val Jcain_v6_MX = Set(Ip6Addr("2600:3c02::f03c:91ff:fe96:dad7"))
  val Jcain_v4_MX_RR = Jcain_v4_MX.foldLeft(Set.empty[A_RR])((set, ipaddr) => set + new A_RR(ipaddr))
  val Jcain_v6_MX_RR = Jcain_v6_MX.foldLeft(Set.empty[AAAA_RR])((set, ipaddr) => set + new AAAA_RR(ipaddr))

  class ResolverFixture(label: String) {
    val resolver = system.actorOf(Props(classOf[Resolver], 1 hour, 10 minutes), s"resolv-$label")
    val testProbe = TestProbe(s"test-$label")
  }

  override def afterAll() {
    system.terminate()
  }

  "Resolver" when {

    "asked a question with no result" when {
      "receives Resolve with A" should {
        "return NotFound" in new ResolverFixture("not-a") {
          resolver.tell(Resolve("blah.jcain.net", A), testProbe.ref)
          testProbe.expectMsg(NotFound("blah.jcain.net", A))

          // stop test
          system.stop(resolver)
        }
      }

      "receives Resolve with AAAA" should {
        "return NotFound" in new ResolverFixture("not-aaaa") {
          resolver.tell(Resolve("blah.jcain.net", AAAA), testProbe.ref)
          testProbe.expectMsg(NotFound("blah.jcain.net", AAAA))

          // stop test
          system.stop(resolver)
        }
      }

      "receives Resolve with MX" should {
        "return NotFound" in new ResolverFixture("not-mx") {
          resolver.tell(Resolve("jcain-domain-not-found.net", MX), testProbe.ref)
          testProbe.expectMsg(NotFound("jcain-domain-not-found.net", MX))

          // stop test
          system.stop(resolver)
        }
      }
    }

    "asked a question with a result" when {
      "receives Resolve with A" should {
        "return A records" in new ResolverFixture("a") {
          resolver.tell(Resolve("mail.jcain.net", A), testProbe.ref)
          testProbe.expectMsg(Result("mail.jcain.net", A, Jcain_v4_MX_RR))

          // stop test
          system.stop(resolver)
        }
      }

      "receives Resolve with AAAA" should {
        "return AAAA records" in new ResolverFixture("aaaa") {
          resolver.tell(Resolve("mail.jcain.net", AAAA), testProbe.ref)
          testProbe.expectMsg(Result("mail.jcain.net", AAAA, Jcain_v6_MX_RR))

          // stop test
          system.stop(resolver)
        }
      }

      "receives Resolve with MX" should {
        "return MX records" in new ResolverFixture("mx") {
          // look up jcain.net MX
          resolver.tell(Resolve("jcain.net", MX), testProbe.ref)
          val result1 = testProbe.expectMsgPF() { case r: Result => r }
          result1.name shouldBe "jcain.net"
          result1.rtype shouldBe MX
          result1.answer shouldBe Set(new MX_RR(10, Map("mail.jcain.net." -> new IpSet(Jcain_v4_MX, Jcain_v6_MX))))

          // look up gmail.com MX
          resolver.tell(Resolve("gmail.com", MX), testProbe.ref)
          val result2 = testProbe.expectMsgPF() { case r: Result => r }
          result2.name shouldBe "gmail.com"
          result2.rtype shouldBe MX
          result2.answer.foreach(rr =>
            rr.asInstanceOf[MX_RR].exchanges.foreach({
              case (host, ipSet) =>
                ipSet.ipv4.foreach(ip4 => ip4 shouldBe a[Ip4Addr])
                ipSet.ipv6.foreach(ip6 => ip6 shouldBe a[Ip6Addr])
            })
          )

          // stop test
          system.stop(resolver)
        }
      }
    }

  }
}
