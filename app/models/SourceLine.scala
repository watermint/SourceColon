package models

import java.io.File
import play.api.libs.json._

case class SourceLine(file: FileMeta,
                      lineNumber: Int,
                      plain: String,
                      printed: Option[String] = None,
                      tokens: Map[SourceToken, Seq[String]] = Map()) {
  lazy val lineId = file.file.toString + "*" + lineNumber
}

object SourceLine {

  implicit object SourceLineFormat extends Format[SourceLine] {
    def reads(js: JsValue): JsResult[SourceLine] = JsSuccess(
      SourceLine(
        FileMeta(new File((js \ "file").as[String])),
        (js \ "line").as[Int],
        (js \ "plain").as[String],
        (js \ "printed").asOpt[String],
        (js \ "token").as[Map[String, Seq[String]]].flatMap {
          t =>
            SourceToken.indexTokensByTag(t._1) match {
              case None => None
              case Some(sourceToken) => Some(sourceToken -> t._2)
            }
        }
      )
    )

    def writes(o: SourceLine): JsValue = JsObject(
      List(
        "file" -> JsString(o.file.file.toString),
        "line" -> JsNumber(o.lineNumber),
        "plain" -> JsString(o.plain),
        "printed" -> (o.printed match {
          case None => JsNull
          case Some(p) => JsString(p)
        }),
        "token" -> JsObject(
          o.tokens.map {
            t =>
              t._1.tag -> JsArray(
                t._2.map {
                  s =>
                    JsString(s)
                }
              )
          }.toList
        )
      )
    )
  }

}
