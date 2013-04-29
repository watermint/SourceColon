package models

import org.watermint.sourcecolon.prettify.Prettify
import java.io.File
import scala.xml.XML
import play.api.libs.json._
import play.api.libs.json.JsSuccess
import scala.Some

/**
 *
 */
object Indexer {

}

trait SearchContent {
  val file: FileMeta
}


case class SourceContent(file: FileMeta)
  extends SearchContent
