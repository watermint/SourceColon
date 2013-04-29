package models


case class SourceToken(name: String, tag: String, shouldIndex: Boolean) {
  override def toString: String = name
}

object SourceToken {
  /**
   * token style for a string literal
   */
  val tokenString = SourceToken(name = "string", tag = "str", shouldIndex = true)

  /**
   * token style for a keyword
   */
  val tokenKeyword = SourceToken(name = "keyword", tag = "kwd", shouldIndex = false)

  /**
   * token style for a comment
   */
  val tokenComment = SourceToken(name = "comment", tag = "com", shouldIndex = true)

  /**
   * token style for a type
   */
  val tokenType = SourceToken(name = "type", tag = "typ", shouldIndex = true)

  /**
   * token style for a literal value.  e.g. 1, null, true.
   */
  val tokenLiteral = SourceToken(name = "literal", tag = "lit", shouldIndex = false)

  /**
   * token style for a punctuation string.
   */
  val tokenPunctuation = SourceToken(name = "punctuation", tag = "pun", shouldIndex = false)

  /**
   * token style for plain text.
   */
  val tokenPlain = SourceToken(name = "plain", tag = "pln", shouldIndex = true)

  /**
   * token style for an sgml tag.
   */
  val tokenTag = SourceToken(name = "tag", tag = "tag", shouldIndex = true)

  /**
   * token style for a markup declaration such as a DOCTYPE.
   */
  val tokenDeclaration = SourceToken(name = "declaration", tag = "dec", shouldIndex = true)

  /**
   * token style for embedded source.
   */
  val tokenSource = SourceToken(name = "source", tag = "src", shouldIndex = true)

  /**
   * token style for an sgml attribute name.
   */
  val tokenAttributeName = SourceToken(name = "attribute name", tag = "atn", shouldIndex = true)

  /**
   * token style for an sgml attribute value.
   */
  val tokenAttributeValue = SourceToken(name = "attribute value", tag = "atv", shouldIndex = true)

  /**
   * tokens
   */
  val tokens = Seq(
    tokenString,
    tokenKeyword,
    tokenComment,
    tokenType,
    tokenLiteral,
    tokenPunctuation,
    tokenPlain,
    tokenTag,
    tokenDeclaration,
    tokenSource,
    tokenAttributeName,
    tokenAttributeValue
  )

  def indexTokensByTag(tag: String): Option[SourceToken] = {
    tokens.find(t => t.tag == tag && t.shouldIndex)
  }
}
