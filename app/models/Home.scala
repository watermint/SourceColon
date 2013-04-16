package models

import play.api.Play
import java.io.File

/**
 * Source: Home Directory
 */
object Home {
  /**
   * Home directory compatibility level.
   */
  lazy val compatibilityLevel = "r4"

  lazy val basePath = Play.current.configuration.getString("sourcecolon.home").
    getOrElse(System.getProperty("user.home") + File.separator + ".sourcecolon")

  lazy val path = basePath + File.separator + compatibilityLevel

  lazy val elasticSearchHome = path + File.separator + "elasticsearch"
}
