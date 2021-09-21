package net.jcain.net

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.must.Matchers

class URISpec extends AnyFunSpec with Matchers {
  class URIFixture(uriString: String) {
    val uri: URI = URI(uriString)
  }

  describe("URI") {
    describe("File") {
      describe("with non-file URI") {
        it("returns None") {
          URI("https://jcain.net/") match {
            case URI.File(path) => assert(false)
            case _ => assert(true)
          }
        }
      }
      describe("with file URI") {
        it("returns the path") {
          URI("/this/is/a/path") match {
            case URI.File(path) =>
              path mustBe "/this/is/a/path"
            case _ => assert(false)
          }
          URI("file:///this/is/a/path") match {
            case URI.File(path) =>
              path mustBe "/this/is/a/path"
            case _ => assert(false)
          }
        }
      }
    }

    describe("Opaque") {
      describe("with non-opaque URI") {
        it("returns None") (new URIFixture("https://jcain.net/") {
          uri match {
            case URI.Opaque(scheme, schemeSpecificPart) => assert(false)
            case _ => assert(true)
          }
          uri.isOpaque mustBe false
        })
      }
      describe("with opaque URI") {
        it("returns the scheme and scheme-specific-part")(new URIFixture("mailto:jeclib@jcain.net") {
          uri match {
            case URI.Opaque(scheme, schemeSpecificPart) =>
              scheme mustBe "mailto"
              schemeSpecificPart mustBe "jeclib@jcain.net"
            case _ => assert(false)
          }
          uri match {
            case URI.Opaque("nosuchscheme", schemeSpecificPart) => assert(false)
            case URI.Opaque("mailto", schemeSpecificPart) =>
              schemeSpecificPart mustBe "jeclib@jcain.net"
            case _ => assert(false)
          }
        })
      }
    }

    describe("Hierarchical") {
      describe("with non-hierarchical URI") {
        it("returns no match") (new URIFixture("mailto:jeclib@jcain.net") {
          uri match {
            case URI.Hierarchical(_, _, _, _, _, _) => assert(false)
            case _ => assert(true)
          }
        })
      }
      describe("with hierarchical URI") {
        it("returns a match") (new URIFixture("https://hostname:1234/path/to/resource?qs=yes#a1") {
          uri match {
            case URI.Hierarchical(scheme, hostname, port, path, query, fragment) =>
              scheme mustBe "https"
              hostname mustBe "hostname"
              port mustBe 1234
              path mustBe "/path/to/resource"
              query mustBe "qs=yes"
              fragment mustBe "a1"
            case _ => assert(false)
          }
        })
      }
    }
  }
}
