package net.jcain.util

import org.scalatest.funspec.AnyFunSpec

class BufferedTokenizerSpec extends AnyFunSpec {
  describe("BufferedTokenizer") {
    it("should split input into tokens on the delimiter") {
      val buffer = new BufferedTokenizer
      val result1 = buffer.extract("This\nis a\ntest\nof").toList
      assert(result1 == List("This", "is a", "test"))
      val result2 = buffer.extract(" the\nemergency").toList
      assert(result2 == List("of the"))
      val result3 = buffer.extract("\nbroadcast\nsystem\n").toList
      assert(result3 == List("emergency", "broadcast", "system"))
    }
  }
}
