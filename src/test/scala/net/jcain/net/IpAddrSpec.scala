package net.jcain.net

import org.scalatest.{FunSpec, Matchers}

class IpAddrSpec extends FunSpec with Matchers {

  describe("IpAddr") {

    describe("apply()") {
      describe("when passed a String") {
        describe("when given an IPv4 address with mask") {
          it("creates an Ip4Addr") {
            val ipaddr = IpAddr("192.168.10.0/24")
            ipaddr shouldBe a[Ip4Addr]
            ipaddr.isIpv4 shouldBe true
            ipaddr.identity shouldBe 32
            ipaddr.isIdentity shouldBe false
          }
        }
        describe("when given an IPv6 address with mask") {
          it("creates an Ip6Addr") {
            val ipaddr = IpAddr("2001:abcd:1234::/48")
            ipaddr shouldBe a[Ip6Addr]
            ipaddr.isIpv6 shouldBe true
            ipaddr.identity shouldBe 128
            ipaddr.isIdentity shouldBe false
          }
        }
      }

      describe("when passed a BigInt") {
        describe("when given a family of 'ipv4") {
          it("creates an Ip4Addr") {
            IpAddr(BigInt("1"), 'ipv4, Some(32)) shouldBe IpAddr("0.0.0.1/32")
            IpAddr(BigInt("2561"), 'ipv4, Some(32)) shouldBe IpAddr("0.0.10.1/32")
            IpAddr(BigInt("11012609"), 'ipv4, Some(32)) shouldBe IpAddr("0.168.10.1/32")
            IpAddr(BigInt("3232238080"), 'ipv4, Some(24)) shouldBe IpAddr("192.168.10.0/24")
          }
        }
        describe("when given a family of 'ipv6") {
          it("creates an Ip6Addr") {
            IpAddr(BigInt("2"), 'ipv6, Some(128)) shouldBe IpAddr("::2/128")
            IpAddr(BigInt("180092930"), 'ipv6, Some(128)) shouldBe IpAddr("::abc:2/128")
            IpAddr(BigInt("11802803896322"), 'ipv6, Some(128)) shouldBe IpAddr("::abc:def:2/128")
            IpAddr(BigInt("773508556168298498"), 'ipv6, Some(128)) shouldBe IpAddr("::abc:def:123:2/128")
            IpAddr(BigInt("50692656737045682978818"), 'ipv6, Some(128)) shouldBe IpAddr("::abc:def:123:456:2/128")
            IpAddr(BigInt("3322193951919025879826104322"), 'ipv6, Some(128)) shouldBe IpAddr("::abc:def:123:456:789:2/128")
            IpAddr(BigInt("217723302832965280060283752808450"), 'ipv6, Some(128)) shouldBe IpAddr("::abc:def:123:456:789:abc:2/128")
            IpAddr(BigInt("14268714374461212594030756027460091906"), 'ipv6, Some(128)) shouldBe IpAddr("abc:def:123:456:789:abc:cafe:2/128")
            IpAddr(BigInt("42543972701424976939786230035910754304"), 'ipv6, Some(48)) shouldBe IpAddr("2001:abcd:1234::/48")
          }
        }
      }
    }

    describe("asBigInt") {
      it("equals the integer value of the address") {
        val ip4 = IpAddr("192.168.10.1/32")
        ip4.asBigInt shouldBe BigInt("3232238081")
        val ip6 = IpAddr("2001:abcd:1234::/48")
        ip6.asBigInt shouldBe BigInt("42543972701424976939786230035910754304")
      }
    }

    describe("contains()") {
      it("indicates whether one range is a subset of another") {
        IpAddr("192.168.10.0/24").contains(IpAddr("192.168.10.99/32")) shouldBe true
        IpAddr("192.168.10.0/24").contains(IpAddr("192.168.11.99/32")) shouldBe false
        IpAddr("192.168.0.0/16").contains(IpAddr("192.168.10.0/24")) shouldBe true
        IpAddr("2001:abcd:1234:5678::/64").contains(IpAddr("2001:abcd:1234:5678:90ab:cdef:1234:5678/128")) shouldBe true
        IpAddr("2001:abcd:1234:5678::/64").contains(IpAddr("2001:abcd:1234:5679:90ab:cdef:1234:5678/128")) shouldBe false
        IpAddr("2001:abcd:1234:5678::/64").contains(IpAddr("2001:abcd:1234:5678:9000::/72")) shouldBe true
      }
    }

    describe("reverse()") {
      it("returns a reverse DNS ARPA domain") {
        IpAddr("192.168.10.123/32").reverse shouldBe "123.10.168.192.in-addr.arpa"
        IpAddr("192.168.0.0/16").reverse shouldBe "0.0.168.192.in-addr.arpa"
        IpAddr("2001:abcd:1234:5678:90ab:cdef:1234:5678/128").reverse shouldBe "8.7.6.5.4.3.2.1.f.e.d.c.b.a.0.9.8.7.6.5.4.3.2.1.d.c.b.a.1.0.0.2.ip6.arpa"
        IpAddr("2001:abcd:1234:5678::/64").reverse shouldBe "8.7.6.5.4.3.2.1.d.c.b.a.1.0.0.2.ip6.arpa"
      }
    }

    describe("subnet()") {
      it("returns a new IpAddr with the specified value added to the IP address") {
        IpAddr("192.168.0.0/16").subnet(Array(10.asInstanceOf[Byte])) shouldBe IpAddr("192.168.0.10/32")
        IpAddr("192.168.0.0/16").subnet(Array(123, 10).map(x => x.asInstanceOf[Byte])) shouldBe IpAddr("192.168.123.10/32")
        IpAddr("2001:abcd:1234:5678::/64").subnet(Array(0x16, 0xfe, 0xab, 0xcd).map(x => x.asInstanceOf[Byte])) shouldBe IpAddr("2001:abcd:1234:5678::16fe:abcd/128")
      }
    }

    describe("random()") {
      it("returns a new IpAddr with a random IP address") {
        val range1 = IpAddr("192.168.0.0/16")
        (0 to 9).foreach((_) => range1.contains(range1.random) shouldBe true)
        val range2 = IpAddr("2001:abcd:1234:5678::/64")
        (0 to 9).foreach((_) => range2.contains(range2.random) shouldBe true)
      }
    }

  }

}
