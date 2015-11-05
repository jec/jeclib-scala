package net.jcain.net

import java.net.InetAddress

object Ip6Addr {

  val maxValue = BigInt(2).pow(128) - 1
  val identity = 128

  def apply(str: String): Ip6Addr = {
    val pieces = str.split("/")
    apply(pieces(0), if (pieces.size == 1) identity else pieces(1).toInt)
  }

  def apply(str: String, mask: Int): Ip6Addr = {
    val bytes = InetAddress.getByName(str).getAddress
    if (bytes.size == 16)
      new Ip6Addr(bytes, mask)
    else
      throw new IllegalArgumentException(s"invalid IPv6 address: $str")
  }

}

class Ip6Addr(bytes: Array[Byte], mask: Int) extends IpAddr(bytes, mask) {

  if (bytes.size != 16) throw new IllegalArgumentException("bytes must be of length 16")
  if (mask < 1 || mask > 128) throw new IllegalArgumentException("mask must be between 1 and 128")

  val inverseMaskAddr = BigInt(2).pow(128 - mask) - 1
  val maskAddr = Ip6Addr.maxValue - inverseMaskAddr

  override def reverse = {
    val nibbleCount = if (mask == 0) 0 else ((mask / 4.0).floor).toInt
    val chars = bytes.foldLeft(List.empty[Char])((list, byte) => {
      val hex = (if (byte < 0) 256 + byte else byte).toHexString.toCharArray
      if (hex.size == 1) hex(0) :: '0' :: list else hex(1) :: hex(0) :: list
    })
    s"${chars.slice(32 - nibbleCount, chars.size).mkString(".")}.ip6.arpa"
  }

  override def isIpv6 = true

  def identity = Ip6Addr.identity

  def family = 'ipv6

  override def equals(other: Any) = other match {
    case that: Ip6Addr => asBigInt == that.asBigInt && mask == that.mask
    case _ => false
  }

  def asString = s"${InetAddress.getByAddress(bytes).getHostAddress}"

  override def toString = s"$asString/$mask"

}
