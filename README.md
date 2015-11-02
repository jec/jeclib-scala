This is my personal Scala library to which I add useful utilities as I work on
projects. I am a newcomer to Scala, so please let me know if I do things which
are not idiomatic.

# BufferedTokenizer

The BufferedTokenizer class extracts text from a stream a chunk at a time,
split by a given delimiter. This is most useful for reading a line at a time
from a buffered stream. This was inspired by the BufferedTokenizer class
included with EventMachine for Ruby.

## Examples

```scala
val buffer = new BufferedTokenizer
println(buffer.extract("This\nis a\ntest\nof").toList)
println(buffer.extract(" the\nemergency").toList)
println(buffer.extract("\nbroadcast\nsystem\n").toList)
```

_produces:_

    List(This, is a, test)
    List(of the)
    List(emergency, broadcast, system)

# IpAddr

The IpAddr class and its subclasses provide IP address manipulation, including
CIDR-style range calculations, for both IPv4 and IPv6 addresses. The methods
provided were loosely inspired by Ruby's IPAddr class, along with some others
I've found useful.

## Examples

```scala
val range = IpAddr("192.168.10.0/24")
val ip = IpAddr("192.168.10.50/32")
println(range)
println(ip)
println(range.contains(ip))
println(range.contains(IpAddr("192.168.11.50/32")))
val v6range = IpAddr("2001:abcd:1234:5678::/64")
println(v6range)
(0 to 2).foreach((_) => println(v6range.random))
```

_produces:_

    192.168.10.0/24
    192.168.10.50/32
    true
    false
    2001:abcd:1234:5678:0:0:0:0/64
    2001:abcd:1234:5678:1354:1bcc:4e88:bc4/128
    2001:abcd:1234:5678:5e05:8c1e:525f:8d6b/128
    2001:abcd:1234:5678:9114:ae51:fd44:b040/128

# License

This library is licensed under the three-clause BSD license (see LICENSE.txt).
