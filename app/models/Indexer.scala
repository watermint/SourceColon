package models

import org.watermint.sourcecolon.prettify.Prettify
import java.io.File
import scala.xml.XML

/**
 *
 */
object Indexer {

}

trait SearchContent {
  val file: FileMeta
}

case class FileMeta(file: File)

case class SourceContent(file: FileMeta)
  extends SearchContent

case class SourceLine(lineNumber: Int,
                      plain: String,
                      prettified: Option[String] = None,
                      tokens: Map[Token, Seq[String]] = Map())

case class SourceLineParser(
                             minimumTokenChars: Int = 2,
                             commentPunctuations: Seq[String] = Seq(
                               """^\/\*\*""",
                               """^\/\*""",
                               """^\*""",
                               """^\#""",
                               """^'""",
                               """\*\/$"""
                             ),
                             stringPunctuations: Seq[String] = Seq(
                               """^\"\"\"""",
                               """\"\"\"$""",
                               """^\"""",
                               """\"$""",
                               """^\'""",
                               """\'$"""
                             )) {

  val prettify = new Prettify

  def filterToken(tokenType: Token, token: String): String = {
    val trimmed = token.trim

    tokenType match {
      case Token.tokenComment =>
        commentPunctuations.foldLeft(trimmed)((t, p) => t.replaceFirst(p, "")).trim
      case Token.tokenString =>
        stringPunctuations.foldLeft(trimmed)((t, p) => t.replaceFirst(p, "")).trim
      case _ =>
        trimmed
    }
  }

  def printedLines(file: FileMeta, plain: String): Seq[String] = {
    val printed = prettify.prettify(plain, prettify.langFromFileName(file.file))
    val printedWithoutRoot = printed.
      replaceFirst( """^<xmp class="([^\"]+)">""", "").
      replaceFirst( """</xmp>$""", "")

    printedWithoutRoot.split("<br/>")
  }

  def printedLineTokens(printedLine: String): Map[Token, Seq[String]] = {
    val x = XML.loadString("<line>" + printedLine + "</line>")

    (x \ "span").map {
      a => (a \ "@class").text -> a.text
    }.groupBy(_._1).flatMap {
      b =>
        Token.indexTokensByTag(b._1) match {
          case None => None
          case Some(t) => {
            val tokens = b._2.map {
              c => filterToken(t, c._2)
            }.filter {
              c => c.length >= minimumTokenChars
            }.distinct
            if (tokens.size > 0) {
              Some((t, tokens))
            } else {
              None
            }
          }
        }
    }
  }

  def plainLines(file: FileMeta, plain: String): Seq[String] = {
    plain.replaceAll("(\r\n|\r)", "\n").split("\n")
  }

  def fromFile(file: FileMeta,
               plain: String): Seq[SourceLine] = {

    val p = plainLines(file, plain)
    val d = printedLines(file, plain)

    if (p.length == d.length) {
      (1 to p.length) map {
        l =>
          SourceLine(
            lineNumber = l,
            plain = p(l - 1),
            prettified = Some(d(l - 1)),
            tokens = printedLineTokens(d(l - 1))
          )
      }
    } else {
      (1 to p.length) map {
        l =>
          SourceLine(
            lineNumber = l,
            plain = p(l - 1)
          )
      }
    }
  }
}

case class Token(name: String, tag: String, shouldIndex: Boolean) {
  override def toString: String = name
}

object Token {
  /**
   * token style for a string literal
   */
  val tokenString = Token(name = "string", tag = "str", shouldIndex = true)

  /**
   * token style for a keyword
   */
  val tokenKeyword = Token(name = "keyword", tag = "kwd", shouldIndex = false)

  /**
   * token style for a comment
   */
  val tokenComment = Token(name = "comment", tag = "com", shouldIndex = true)

  /**
   * token style for a type
   */
  val tokenType = Token(name = "type", tag = "typ", shouldIndex = true)

  /**
   * token style for a literal value.  e.g. 1, null, true.
   */
  val tokenLiteral = Token(name = "literal", tag = "lit", shouldIndex = false)

  /**
   * token style for a punctuation string.
   */
  val tokenPunctuation = Token(name = "punctuation", tag = "pun", shouldIndex = false)

  /**
   * token style for plain text.
   */
  val tokenPlain = Token(name = "plain", tag = "pln", shouldIndex = true)

  /**
   * token style for an sgml tag.
   */
  val tokenTag = Token(name = "tag", tag = "tag", shouldIndex = true)

  /**
   * token style for a markup declaration such as a DOCTYPE.
   */
  val tokenDeclaration = Token(name = "declaration", tag = "dec", shouldIndex = true)

  /**
   * token style for embedded source.
   */
  val tokenSource = Token(name = "source", tag = "src", shouldIndex = true)

  /**
   * token style for an sgml attribute name.
   */
  val tokenAttributeName = Token(name = "attribute name", tag = "atn", shouldIndex = true)

  /**
   * token style for an sgml attribute value.
   */
  val tokenAttributeValue = Token(name = "attribute value", tag = "atv", shouldIndex = true)

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

  def indexTokensByTag(tag: String): Option[Token] = {
    tokens.find(t => t.tag == tag && t.shouldIndex)
  }
}
