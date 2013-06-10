package models

import org.specs2.mutable.Specification
import java.io.File
import scala.io.Source
import play.api.libs.json.Json

/**
 *
 */
class SourceFileSpec extends Specification {
  "SourceFile" should {
    "serialized/deserialized to JSON" in {
      val slp = SourceLineParser()
      val file = new File("app/models/SourceLineParser.scala")
      val src = Source.fromFile(file)
      val plain = src.getLines().toList.mkString("\n")
      src.close()

      val content = slp.fromFile(FileMeta(file), plain)

      val f = SourceFile(
        FileMeta(file),
        content.flatMap(_.printed)
      )

      val js = Json.toJson(f)
      val o = Json.fromJson[SourceFile](js)

      o.get must equalTo(f)
    }
  }
}
