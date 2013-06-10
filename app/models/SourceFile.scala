package models

import java.io.File
import play.api.libs.json._

case class SourceFile(file: FileMeta,
                      lines: Seq[String]) {
  def fileId = file.toString
}

object SourceFile {

  implicit object SourceFileFormat extends Format[SourceFile] {
    def reads(j: JsValue): JsResult[SourceFile] = JsSuccess(
      SourceFile(
        FileMeta(new File((j \ "file").as[String])),
        (j \ "lines").as[Seq[String]]
      )
    )

    def writes(o: SourceFile): JsValue = JsObject(
      List(
        "file" -> JsString(o.file.file.toString),
        "lines" -> JsArray(
          o.lines.map {
            l =>
              JsString(l)
          }
        )
      )
    )
  }

}