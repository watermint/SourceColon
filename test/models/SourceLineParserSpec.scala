package models

import org.specs2.mutable.Specification
import java.io.File
import scala.io.Source

/**
 *
 */
class SourceLineParserSpec extends Specification {
  "SourceLineParser" should {
    "printed line should parsed as tokens" in {
      val slp = SourceLineParser()

      slp.printedLineTokens("""<span class="pln">source!</span>""") must equalTo(
        Map(SourceToken.tokenPlain -> Seq("source!")))

      // Tokens below should be ignored
      slp.printedLineTokens("""<span class="kwd">source!</span>""") must equalTo(Map())
      slp.printedLineTokens("""<span class="pun">source!</span>""") must equalTo(Map())
      slp.printedLineTokens("""<span class="lit">source!</span>""") must equalTo(Map())
    }

    "parse source code" in {
      val slp = SourceLineParser()
      val file = new File("app/models/SourceLineParser.scala")
      val src = Source.fromFile(file)
      val plain = src.getLines().toList.mkString("\n")
      src.close()

      val content = slp.fromFile(FileMeta(file), plain)

      content.foreach {
        c =>
          printf("%03d: ", c.lineNumber)
          printf("%-30s ", c.plain.substring(0, 29 min c.plain.length))
          c.printed foreach {
            p =>
              printf("| %-50s ", p.substring(0, 49 min p.length))
              printf("| %s", c.tokens.toString())
          }
          println("")
      }

      content.size must greaterThan(1)
      content.seq.find {
        c =>
          c.tokens.getOrElse(SourceToken.tokenType, Seq()).find {
            t =>
              t == "SourceLineParser"
          }.isDefined
      }.isDefined must beTrue

    }
  }
}
