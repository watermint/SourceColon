import models.search.Engine
import play.api._

/**
 *
 */
object SourceColon extends GlobalSettings {
  override def onStart(app: Application) {
    super.onStart(app)

    Engine.startup()
  }

  override def onStop(app: Application) {
    super.onStop(app)

    Engine.shutdown()
  }
}
