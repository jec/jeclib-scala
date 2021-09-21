package net.jcain.util

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class HexDumperSpec extends AnyWordSpec with Matchers {
  val Test1 = List(
    "00000000  54 68 69 73 20 69 73 20  61 20 74 65 73 74 2e 0d  |This is a test..|",
    "00000010  0a                                                |.|"
  )

  val Test2 = List(
    "00000000  54 68 69 73 20 69 73 20  61 20 74 65 73 74 0d 0a  |This is a test..|",
    "00000010  6f 66 20 74 68 65 20 65  6d 65 72 67 65 6e 63 79  |of the emergency|",
    "00000020  0d 0a 62 72 6f 61 64 63  61 73 74 20 73 79 73 74  |..broadcast syst|",
    "00000030  65 6d 2e 0d 0a 54 68 69  73 20 69 73 20 6f 6e 6c  |em...This is onl|",
    "00000040  79 20 61 0d 0a 74 65 73  74 2e 0d 0a              |y a..test...|"
  )

  val Test3Array = Array[Byte](-39, 54, 51, 28, -75, -77, -85, -103, 96, -3, 55, 18, 99, 99, 68, 88, -71, -125, -14, 10, 0, -51, -101, -26, 36, 57, 87, 65, 116, 85, -13, 61, -77, 55, 48, 103, -61, 41, -61, 71, 98, 47, 55, -90, -5, 33, -117, -115)

  val Test3 = List(
    "00000000  d9 36 33 1c b5 b3 ab 99  60 fd 37 12 63 63 44 58  |.63.....`.7.ccDX|",
    "00000010  b9 83 f2 0a 00 cd 9b e6  24 39 57 41 74 55 f3 3d  |........$9WAtU.=|",
    "00000020  b3 37 30 67 c3 29 c3 47  62 2f 37 a6 fb 21 8b 8d  |.70g.).Gb/7..!..|"
  )

  "HexDumper" when {
    "dump()" should {
      "output a hex dump" in {
        HexDumper.dump("This is a test.\r\n".getBytes) mustBe Test1
        HexDumper.dump("This is a test\r\nof the emergency\r\nbroadcast system.\r\nThis is only a\r\ntest.\r\n".getBytes) mustBe Test2
        HexDumper.dump(Test3Array) mustBe Test3
      }
    }
  }
}
