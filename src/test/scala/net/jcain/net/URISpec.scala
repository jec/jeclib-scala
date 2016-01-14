package net.jcain.net

import org.scalatest.{Matchers, FunSpec}

class URISpec extends FunSpec with Matchers {

  class URIFixture(uriString: String) {
    val uri = URI(uriString)
  }

  describe("URI") {

    describe("File") {
      describe("with non-file URI") {
        it("returns None") {
          URI("http://jcain.net/") match {
            case URI.File(path) => assert(false)
            case _ => assert(true)
          }
        }
      }
      describe("with file URI") {
        it("returns the path") {
          URI("/this/is/a/path") match {
            case URI.File(path) =>
              path shouldBe "/this/is/a/path"
            case _ => assert(false)
          }
          URI("file:///this/is/a/path") match {
            case URI.File(path) =>
              path shouldBe "/this/is/a/path"
            case _ => assert(false)
          }
        }
      }
    }

    describe("Opaque") {
      describe("with non-opaque URI") {
        it("returns None") (new URIFixture("http://jcain.net/") {
          uri match {
            case URI.Opaque(scheme, schemeSpecificPart) => assert(false)
            case _ => assert(true)
          }
          uri.isOpaque shouldBe false
        })
      }
      describe("with opaque URI") {
        it("returns the scheme and scheme-specific-part")(new URIFixture("mailto:jeclib@jcain.net") {
          uri match {
            case URI.Opaque(scheme, schemeSpecificPart) =>
              scheme shouldBe "mailto"
              schemeSpecificPart shouldBe "jeclib@jcain.net"
            case _ => assert(false)
          }
          uri match {
            case URI.Opaque("nosuchscheme", schemeSpecificPart) => assert(false)
            case URI.Opaque("mailto", schemeSpecificPart) =>
              schemeSpecificPart shouldBe "jeclib@jcain.net"
            case _ => assert(false)
          }
        })
      }
    }

  }
}
