package net.jcain.net

object Ip4Addr {

  val maxValue = BigInt(2).pow(32) - 1
  val identity = 32

}

class Ip4Addr(bytes: Array[Byte], mask: Int) extends IpAddr(bytes, mask) {

  if (bytes.size != 4) throw new IllegalArgumentException("bytes must be of length 4")
  if (mask < 1 || mask > 128) throw new IllegalArgumentException("mask must be between 1 and 128")

  val inverseMaskAddr = BigInt(2).pow(32 - mask) - 1
  val maskAddr = Ip4Addr.maxValue - inverseMaskAddr

  override def reverse = {
    val list = bytes.reverse.map(b => if (b < 0) 256 + b else b)
    s"${list.mkString(".")}.in-addr.arpa"
  }

  override def isIpv4 = true

  def identity = Ip4Addr.identity

  def family = 'ipv4

  override def equals(other: Any) = other match {
    case that: Ip4Addr => asBigInt == that.asBigInt && mask == that.mask
    case _ => false
  }

  override def toString = s"${bytes.map(b => if (b < 0) 256 + b else b).mkString(".")}/$mask"

}
