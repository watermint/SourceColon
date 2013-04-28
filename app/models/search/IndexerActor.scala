package models.search

import akka.actor.{Props, ActorSystem, Actor}
import java.io.{BufferedInputStream, FileInputStream, StringWriter, File}
import play.api.Logger
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler

case class IndexTask(file: File, basePath: File)

case class IndexContent(file: File)

class IndexerActor extends Actor {
  def receive = {
    case task: IndexContent => {
      val meta = new Metadata()
      val parser = new AutoDetectParser()
      val writer = new StringWriter()
      val contentHandler = new BodyContentHandler(writer)
      val fileContent = new FileInputStream(task.file)
      val inputStream = new BufferedInputStream(fileContent)

      try {
        parser.parse(inputStream, contentHandler, meta)
      } finally {
        fileContent.close()
      }

      println("\n\n")
      println("---- " + task.file.getCanonicalPath)
      meta.names().foreach {
        n =>
          println(n + " = " + meta.get(n))
      }

      val contentType = meta.get("Content-type")
    }
    case task: IndexTask => {
      if (task.file.isDirectory) {
        task.file.listFiles().foreach {
          f =>
            IndexerActor.ref ! IndexTask(f, task.basePath)
        }
      } else {
        val docId = ManagedFile.relativePath(task.file, task.basePath)

        IndexerActor.ref ! IndexContent(task.file)

        Logger.info("Index file: " + docId)
        Engine.get[ManagedFile]("files", "file", docId) match {
          case Some(mf) =>
            Logger.debug("Index exists: " + docId)
          case None =>
            Engine.set("files", "file", docId, ManagedFile(task.file, task.basePath))
        }
      }
    }
  }
}

object IndexerActor {
  lazy val system = ActorSystem("indexer")

  lazy val ref = system.actorOf(Props[IndexerActor])
}
