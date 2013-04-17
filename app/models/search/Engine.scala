package models.search

import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.{NodeBuilder, Node}
import models.Home
import play.api.libs.json.{Writes, Json, Reads}
import org.apache.lucene.index.IndexNotFoundException
import play.api.Logger
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.action.search.SearchPhaseExecutionException

object Engine {

  case class IndexResult(docType: String, docId: String, version: Long)

  case class GetResult[T <: ManagedObject](docType: String, docId: String, version: Long, content: T)

  lazy val node: Node = NodeBuilder.nodeBuilder().local(false).settings(
    ImmutableSettings.settingsBuilder().put("path.home", Home.elasticSearchHome)
  ).node()

  lazy val client: Client = node.client()

  def startup() {
    client.admin()
  }

  def shutdown() {
    node.close()
  }

  def set[T <: ManagedObject](docIndex: String, docType: String, docId: String, content: T)(implicit writes: Writes[T]): Option[IndexResult] = {
    val json = Json.toJson(content).toString()
    val response = client.prepareIndex(docIndex, docType, docId).
      setSource(json).
      execute().
      actionGet()

    Some(
      IndexResult(
        response.getType,
        response.getId,
        response.getVersion
      )
    )
  }

  def get[T <: ManagedObject](docIndex: String, docType: String, docId: String)(implicit reads: Reads[T]): Option[GetResult[T]] = {
    try {
      val response = client.prepareGet(docIndex, docType, docId).execute().actionGet()
      if (response.isExists) {
        reads.reads(Json.parse(response.getSourceAsString)).asOpt match {
          case Some(t) =>
            Some(
              GetResult(
                response.getType,
                response.getId,
                response.getVersion,
                t
              )
            )
          case None =>
            None
        }
      } else {
        None
      }
    } catch {
      case _: IndexNotFoundException => None
      case e: Throwable =>
        Logger.debug(e.toString)
        None
    }
  }

  def search[T <: ManagedObject](docIndex: String, docType: String, query: QueryBuilder)(implicit reads: Reads[T]): Option[Seq[T]] = {
    try {
      val response = client.prepareSearch(docIndex).
        setTypes(docType).
        setQuery(query).
        execute().
        actionGet()

      Some(
        response.getHits.hits().map {
          h =>
            reads.reads(Json.parse(h.getSourceAsString)).asOpt
        }.filter(_.isDefined).map(_.get)
      )
    } catch {
      case _: SearchPhaseExecutionException => None
      case e: Throwable =>
        Logger.debug(e.toString)
        None
    }
  }
}
