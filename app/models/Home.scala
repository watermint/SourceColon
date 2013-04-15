package models

import play.api.Play
import java.io.File

/**
 * Source: Home Directory
 */
object Home {
  lazy val basePath = Play.current.configuration.getString("sourcecolon.home") match {
    case Some(h) => h
    case None =>
      System.getProperty("user.home") + File.separator + ".sourcecolon"
  }

  lazy val release = Play.current.configuration.getString("sourcecolon.compatibility").getOrElse("dev")

  lazy val path = basePath + File.separator + release

  lazy val elasticSearchHome = path + File.separator + "elasticsearch"
}
