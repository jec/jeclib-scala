package net.jcain.net

import java.net.InetAddress

object IpAddr {

  val Families = Map('ipv4 -> (Ip4Addr.maxValue, 32, 4), 'ipv6 -> (Ip6Addr.maxValue, 128, 16))

  def apply(str: String): IpAddr = {
    val pieces = str.split("/")
    apply(pieces(0), if (pieces.size == 0) 0 else pieces(1).toInt)
  }

  def apply(str: String, mask: Int): IpAddr = {
    val bytes = InetAddress.getByName(str).getAddress
    if (bytes.size == 4)
      new Ip4Addr(bytes, if (mask == 0) Ip4Addr.identity else mask)
    else
      new Ip6Addr(bytes, if (mask == 0) Ip6Addr.identity else mask)
  }

  def apply(int: BigInt, family: Symbol, maskp: Option[Int] = None): IpAddr = {
    val (baseMask, maxBits, byteCount) = Families.getOrElse(family, throw new IllegalArgumentException("family must be 'ipv4 or 'ipv6"))
    val mask = maskp.getOrElse(maxBits)
    val valueMask = (baseMask << (maxBits - mask)) & baseMask
    val ipInt = int & valueMask
    val bytes = ipInt.toByteArray
    // for IPv4 values, toByteArray() returns 5 elements; for IPv6 it returns
    // 16 elements
    val adjBytes = if (family == 'ipv4 && bytes.size == 5) bytes.tail else bytes
    // pad as necessary to the proper byte count for the family
    val allBytes = new Array[Byte](byteCount - adjBytes.size) ++ adjBytes
    if (family == 'ipv4) new Ip4Addr(allBytes, mask) else new Ip6Addr(allBytes, mask)
  }

  def toBigInt(bytes: Array[Byte]): BigInt =
    bytes.foldLeft(BigInt(0))((sum, byte) => (sum << 8) + (if (byte < 0) 256 + byte else byte))

  def getMaxBigInt(bytes: Array[Byte], mask: Int, maxBits: Int): BigInt = {
    val maxMask = BigInt(2).pow(maxBits - mask) - 1
    toBigInt(bytes) | maxMask
  }

  val rgen = new java.security.SecureRandom

}

abstract class IpAddr(val bytes: Array[Byte], val mask: Int) {

  val asBigInt = IpAddr.toBigInt(bytes)
  val maxBigInt = IpAddr.getMaxBigInt(bytes, mask, identity)
  val maskAddr: BigInt
  val inverseMaskAddr: BigInt

  def contains(that: IpAddr): Boolean =
    asBigInt <= that.asBigInt && that.asBigInt <= maxBigInt && asBigInt <= that.maxBigInt && that.maxBigInt <= maxBigInt

  def subnet(int: BigInt) = IpAddr(asBigInt + (int & inverseMaskAddr), family, Some(identity))

  def subnet(bytes: Array[Byte]) = IpAddr(asBigInt + (IpAddr.toBigInt(bytes) & inverseMaskAddr), family, Some(identity))

  def random = if (isIdentity) this else {
    val ary = new Array[Byte](((identity - mask) / 8.0).ceil.toInt)
    IpAddr.rgen.nextBytes(ary)
    subnet(ary)
  }

  def reverse: String

  def isIpv4 = false

  def isIpv6 = false

  def identity: Int

  def isIdentity = mask == identity

  def family: Symbol

  override def hashCode = 41 * (41 * (41 + asBigInt.hashCode) + mask)

}
