package controllers

import models.search.{Engine, ManagedFile, IndexTask, IndexerActor}
import java.io.File

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.elasticsearch.index.query.QueryBuilders

object FileFinder extends Controller {
  val filesForm = Form(
    "q" -> optional(text)
  )

  def jsRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.FileFinder.files
        )
      ).as("text/javascript")
  }

  def enqueue(path: String) = Action {
    implicit request =>
      var f = new File(path)
      if (f.exists()) {
        IndexerActor.ref ! IndexTask(f, f)
        Ok(views.html.index("Queued"))
      } else {
        Ok(views.html.index("Path not found"))
      }
  }

  def index = Action {
    implicit request =>
      Ok(views.html.index(""))
  }

  def files(query: String) = Action {
    val fuzzy = query.toList.mkString("*")
    Ok(
      views.html.files(
        Engine.search[ManagedFile](
          "files",
          "file",
          QueryBuilders.wildcardQuery("path", "*" + fuzzy + "*")
        ).getOrElse(Seq())
      )
    )
  }
}