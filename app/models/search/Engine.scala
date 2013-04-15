package models.search

import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.node.{NodeBuilder, Node}
import models.Home

/**
 *
 */
object Engine {
  lazy val node: Node = NodeBuilder.nodeBuilder().local(true).settings(
    ImmutableSettings.settingsBuilder().put("path.home", Home.elasticSearchHome)
  ).build()

  lazy val client: Client = node.client()

  def startup() {
    node.start()
  }

  def shutdown() {
    node.close()
  }
}
