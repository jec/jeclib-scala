package net.jcain.net

object URI {

  object SchemeSpecific {
    def unapply(uri: URI): Option[(String, String)] =
      if (uri.isSchemeSpecificOnly) Some((uri.scheme, uri.schemeSpecificPart))
      else None
  }

  object File {
    def unapply(uri: URI): Option[String] =
      if (uri.isFile) Some(uri.path)
      else None
  }

}

class URI(uri: String) {

  val _uri = new java.net.URI(uri)
  val scheme = _uri.getScheme
  val schemeSpecificPart = _uri.getSchemeSpecificPart
  val user = _uri.getUserInfo
  val host = _uri.getHost
  val port = _uri.getPort
  val path = _uri.getPath
  val query = _uri.getQuery
  val fragment = _uri.getFragment

  def isSchemeSpecificOnly =
    scheme != null && schemeSpecificPart != null && user == null && host == null && port == -1 && path == null

  def isFile =
    (scheme == "file") || (scheme == null && path != null)

  override def toString = _uri.toString

}
