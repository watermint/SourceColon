package controllers

import play.api.mvc.{Action, Controller}
import play.api.Routes
import models.search.{ManagedFile, Engine}
import org.elasticsearch.index.query.QueryBuilders
import models.{SourceToken, SourceLine}

/**
 *
 */
object LineSearch extends Controller {
  def jsRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.LineSearch.lines
        )
      ).as("text/javascript")
  }

  def index = Action {
    implicit request =>
      Ok(views.html.line_search(""))
  }

  def lines(query: String) = Action {
    val searchableTokens = SourceToken.tokens.map {c => c.tag}
    val tokens: Seq[String] = searchableTokens :+ "plain"
    Ok(
      views.html.line(
        Engine.search[SourceLine](
          "lines",
          "line",
          QueryBuilders.fuzzyLikeThisQuery(tokens: _*).likeText(query),
          0,
          30
        ).getOrElse(Seq())
      )
    )
  }
}
