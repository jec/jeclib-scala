package net.jcain.util

class BufferedTokenizer(delimiter: String = "\n") {

  import scala.collection.mutable.ListBuffer

  // Contains the data awaiting the next delimiter
  val input = new ListBuffer[String]

  def extract(data: String): Iterator[String] = {
    val entities = new ListBuffer[String] ++= data.split(delimiter, -1)
    input += entities.remove(0)
    if (entities.isEmpty) {
      Iterator.empty
    } else {
      entities.prepend(input.mkString)
      input.clear()
      input += entities.remove(entities.size - 1)
      entities.iterator
    }
  }

}
