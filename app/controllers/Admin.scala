package controllers

import play.api.mvc._
import play.Play
import scala.io.Source
import org.watermint.sourcecolon.prettify.Prettify

object Admin extends Controller {
  def index = Action {
    Ok(views.html.index(""))
  }
}