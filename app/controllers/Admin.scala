package controllers

import models.search.{Engine, ManagedFile, IndexTask, IndexerActor}
import java.io.File

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import org.elasticsearch.index.query.QueryBuilders
import play.api.templates.Html

object Admin extends Controller {
  val filesForm = Form(
    "q" -> optional(text)
  )

  def index = Action {
    IndexerActor.ref ! IndexTask(new File("."), new File("."))
    Ok(views.html.index())
  }

  def files = Action {
    implicit request =>
      filesForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.main("error")(Html(""))), {
        case (q) => {

          Ok(
            views.html.index(
              Engine.search[ManagedFile](
                "files",
                "file",
                QueryBuilders.wildcardQuery("path", q.getOrElse(""))
              ).getOrElse(Seq())
            )
          )
        }
      }
      )
  }
}