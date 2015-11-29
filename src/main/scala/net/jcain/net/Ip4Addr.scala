package net.jcain.net

import java.net.InetAddress

object Ip4Addr {

  val maxValue = BigInt(2).pow(32) - 1
  val identity = 32

  def apply(str: String): Ip4Addr = {
    val pieces = str.split("/")
    apply(pieces(0), if (pieces.size == 1) identity else pieces(1).toInt)
  }

  def apply(str: String, mask: Int): Ip4Addr = {
    val bytes = InetAddress.getByName(str).getAddress
    if (bytes.length == 4)
      new Ip4Addr(bytes, mask)
    else
      throw new IllegalArgumentException(s"invalid IPv4 address: $str")
  }

}

class Ip4Addr(bytes: Array[Byte], mask: Int) extends IpAddr(bytes, mask) {

  if (bytes.length != 4) throw new IllegalArgumentException("bytes must be of length 4")
  if (mask < 1 || mask > 128) throw new IllegalArgumentException("mask must be between 1 and 128")

  val inverseMaskAddr = BigInt(2).pow(32 - mask) - 1
  val maskAddr = Ip4Addr.maxValue - inverseMaskAddr

  override def reverse = {
    val list = bytes.reverse.map(_.toInt).map(b => if (b < 0) 256 + b else b)
    s"${list.mkString(".")}.in-addr.arpa"
  }

  override def isIpv4 = true

  def identity = Ip4Addr.identity

  def family = 'ipv4

  override def equals(other: Any) = other match {
    case that: Ip4Addr => asBigInt == that.asBigInt && mask == that.mask
    case _ => false
  }

  def asString = s"${bytes.map(_.toInt).map(b => if (b < 0) 256 + b else b).mkString(".")}"

  override def toString = s"$asString/$mask"

}
