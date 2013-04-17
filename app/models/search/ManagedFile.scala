package models.search

import java.io.File
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.JsSuccess

trait ManagedObject

case class ManagedFile(path: String,
                       name: String,
                       directory: String,
                       size: Long,
                       updated: DateTime) extends ManagedObject {

}

object ManagedFile {
  def apply(file: File, basePath: File): ManagedFile = {
    ManagedFile(
      relativePath(file, basePath),
      file.getName,
      file.getParentFile.getCanonicalPath,
      file.length(),
      new DateTime(file.lastModified())
    )
  }

  def relativePath(file: File, basePath: File): String = {
    file.getCanonicalPath.replaceFirst("^" + basePath.getCanonicalPath, "")
  }

  implicit object ManagedFileFormat extends Format[ManagedFile] {
    def reads(json: JsValue): JsResult[ManagedFile] = JsSuccess(
      ManagedFile(
        (json \ "path").as[String],
        (json \ "name").as[String],
        (json \ "directory").as[String],
        (json \ "size").as[Long],
        DateTime.parse((json \ "updated").as[String])
      )
    )

    def writes(o: ManagedFile): JsValue = JsObject(
      List(
        "path" -> JsString(o.path),
        "name" -> JsString(o.name),
        "directory" -> JsString(o.directory),
        "size" -> JsNumber(o.size),
        "updated" -> JsString(o.updated.toString)
      )
    )
  }

}
