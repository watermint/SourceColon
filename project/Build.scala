import sbt._
import sbt.Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "SourceColon"
  val appVersion = "r4"

  val appDependencies = Seq(
    jdbc,
    anorm,
    "org.apache.tika" % "tika-parsers" % "1.3",
    "org.elasticsearch" % "elasticsearch" % "0.20.6",
    "org.watermint" % "sourcecolon-code-prettify" % "r1"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "watermint.org Repository" at "http://watermint.org/mvn"
  )
}
