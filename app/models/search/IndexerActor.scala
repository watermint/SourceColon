package models.search

import akka.actor.{Props, ActorSystem, Actor}
import java.io.File
import play.api.Logger

case class IndexTask(file: File, basePath: File)

class IndexerActor extends Actor {
  def receive = {
    case task: IndexTask => {
      if (task.file.isDirectory) {
        task.file.listFiles().foreach {
          f =>
            IndexerActor.ref ! IndexTask(f, task.basePath)
        }
      } else {
        val docId = ManagedFile.relativePath(task.file, task.basePath)

        Logger.info("Index file: " + docId)
        Engine.get[ManagedFile]("files", "file", docId) match {
          case Some(mf) =>
            Logger.debug("Index exists: " + docId)
          case None =>
            Engine.index("files", "file", docId, ManagedFile(task.file, task.basePath))
        }
      }
    }
  }
}

object IndexerActor {
  lazy val system = ActorSystem("indexer")

  lazy val ref = system.actorOf(Props[IndexerActor])
}
