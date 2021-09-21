package net.jcain.net

object URI {
  def apply(uri: String) = new URI(uri)

  object Opaque {
    def unapply(uri: URI): Option[(String, String)] =
      if (uri.isOpaque) Some((uri.scheme, uri.schemeSpecificPart))
      else None
  }

  // this is a subset of Hierarchical
  object File {
    def unapply(uri: URI): Option[String] =
      if (uri.isFile) Some(uri.path)
      else None
  }

  // this is a superset of File
  object Hierarchical {
    def unapply(uri: URI): Option[(String, String, Int, String, String, String)] =
      if (!uri.isOpaque) Some((uri.scheme, uri.host, uri.port, uri.path, uri.query, uri.fragment))
      else None
  }
}

class URI(uri: String) {
  val _uri = new java.net.URI(uri)
  val scheme: String = _uri.getScheme
  val schemeSpecificPart: String = _uri.getSchemeSpecificPart
  val user: String = _uri.getUserInfo
  val host: String = _uri.getHost
  val port: Int = _uri.getPort
  val path: String = _uri.getPath
  val query: String = _uri.getQuery
  val fragment: String = _uri.getFragment

  def isFile: Boolean =
    (scheme == "file") || (scheme == null && path != null)

  def isOpaque: Boolean = _uri.isOpaque

  override def toString: String = _uri.toString
}
