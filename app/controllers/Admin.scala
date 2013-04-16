package controllers

import play.api.mvc._
import models.search.{IndexTask, IndexerActor}
import java.io.File

object Admin extends Controller {
  def index = Action {
    IndexerActor.ref ! IndexTask(new File("."), new File("."))
    Ok(views.html.index("hello"))
  }
}