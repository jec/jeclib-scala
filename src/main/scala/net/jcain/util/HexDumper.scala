package net.jcain.util

object HexDumper {

  def dump(bytes: Array[Byte]): Seq[String] = {
    val arrays = bytes.sliding(16, 16).toList.map(_.map(_.toInt).map(x => if (x < 0) x + 256 else x))
    val hex = arrays.map(_.map("%02x".format(_))).map(a => (a.slice(0, 8).mkString(" "), a.slice(8, 16).mkString(" ")))
    val text = arrays.map(_.map(x => if (32 <= x && x <= 126) x.toChar else '.')).map(c => s"|${new String(c)}|")
    hex.zip(text).zipWithIndex.map { case (((a1, a2), s), i) => "%08x  %-23s  %-23s  %s".format(i * 16, a1, a2, s)}
  }

}
