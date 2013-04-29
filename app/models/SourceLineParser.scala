package models

import org.watermint.sourcecolon.prettify.Prettify
import scala.xml.XML


case class SourceLineParser(minimumTokenChars: Int = 2,
                            commentPunctuations: Seq[String] = SourceLineParser.defaultCommentPunctuations,
                            stringPunctuations: Seq[String] = SourceLineParser.defaultStringPunctuations) {

  val prettify = new Prettify

  def filterToken(tokenType: SourceToken, token: String): String = {
    val trimmed = token.trim

    tokenType match {
      case SourceToken.tokenComment =>
        commentPunctuations.foldLeft(trimmed)((t, p) => t.replaceFirst(p, "")).trim
      case SourceToken.tokenString =>
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

  def printedLineTokens(printedLine: String): Map[SourceToken, Seq[String]] = {
    val x = XML.loadString("<line>" + printedLine + "</line>")

    (x \ "span").map {
      a => (a \ "@class").text -> a.text
    }.groupBy(_._1).flatMap {
      b =>
        SourceToken.indexTokensByTag(b._1) match {
          case None => None
          case Some(t) => {
            val tokens = b._2.map {
              c => filterToken(t, c._2)
            }.filter {
              c => c.length >= minimumTokenChars
            }.distinct

            if (tokens.size > 0) {
              Some(t -> tokens)
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
            file = file,
            lineNumber = l,
            plain = p(l - 1),
            printed = Some(d(l - 1)),
            tokens = printedLineTokens(d(l - 1))
          )
      }
    } else {
      (1 to p.length) map {
        l =>
          SourceLine(
            file = file,
            lineNumber = l,
            plain = p(l - 1)
          )
      }
    }
  }
}

object SourceLineParser {
  lazy val defaultCommentPunctuations = Seq(
    """^\/\*\*""",
    """^\/\*""",
    """^\*""",
    """^\#""",
    """^'""",
    """\*\/$"""
  )

  lazy val defaultStringPunctuations = Seq(
    """^\"\"\"""",
    """\"\"\"$""",
    """^\"""",
    """\"$""",
    """^\'""",
    """\'$"""
  )
}
