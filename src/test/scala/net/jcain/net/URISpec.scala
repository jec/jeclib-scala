package net.jcain.net

import org.scalatest.{Matchers, FunSpec}

class URISpec extends FunSpec with Matchers {

  describe("URI") {

    describe("File") {
      describe("with non-file URI") {
        it("returns None") {
          new URI("http://tremtek.com/") match {
            case URI.File(path) => assert(false)
            case _ => assert(true)
          }
        }
      }
      describe("with file URI") {
        it("returns the path") {
          new URI("/this/is/a/path") match {
            case URI.File(path) =>
              path shouldBe "/this/is/a/path"
            case _ => assert(false)
          }
          new URI("file:///this/is/a/path") match {
            case URI.File(path) =>
              path shouldBe "/this/is/a/path"
            case _ => assert(false)
          }
        }
      }
    }

    describe("SchemeSpecific") {
      describe("with non-scheme-specific URI") {
        it("returns None") {
          new URI("http://tremtek.com/") match {
            case URI.SchemeSpecific(scheme, schemeSpecificPart) => assert(false)
            case _ => assert(true)
          }
        }
      }
      describe("with scheme-specific URI") {
        it("returns the scheme and scheme-specific-part") {
          new URI("centcom:get_next_job") match {
            case URI.SchemeSpecific(scheme, schemeSpecificPart) =>
              scheme shouldBe "centcom"
              schemeSpecificPart shouldBe "get_next_job"
            case _ => assert(false)
          }
          new URI("centcom:get_next_job") match {
            case URI.SchemeSpecific("blah", schemeSpecificPart) => assert(false)
            case URI.SchemeSpecific("centcom", schemeSpecificPart) =>
              schemeSpecificPart shouldBe "get_next_job"
            case _ => assert(false)
          }
        }
      }
    }

  }
}
