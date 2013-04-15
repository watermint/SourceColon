package controllers

import play.api.mvc._

object Admin extends Controller {
  def index = Action {
    Ok(views.html.index(""))
  }
}