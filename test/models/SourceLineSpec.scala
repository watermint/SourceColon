package models

import org.specs2.mutable.Specification
import java.io.File
import scala.io.Source
import play.api.libs.json.Json

/**
 *
 */
class SourceLineSpec extends Specification {
  "SourceLine" should {
    "Serialized/deserialized into JSON" in {
      "parse source code" in {
        val slp = SourceLineParser()
        val file = new File("app/models/SourceLineParser.scala")
        val src = Source.fromFile(file)
        val plain = src.getLines().toList.mkString("\n")
        src.close()

        val content = slp.fromFile(FileMeta(file), plain)

        content.foreach {
          c =>
            val js = Json.toJson(c)
            val o = Json.fromJson[SourceLine](js)

            o.get must equalTo(c)
        }
      }
    }
  }
}
