import sbt._
import Keys._

import bintray.Plugin._

object FirkinBuild  extends Build {
  val VERSION = "0.1.2"
  
  lazy val common = project settings(commonSettings ++ bintrayPublishSettings: _*)

  lazy val server = project settings(serverSettings : _*) dependsOn(common, client)
  
  lazy val client = project settings(clientSettings ++ bintrayPublishSettings: _*)
    
  lazy val root = (project in file(".")).aggregate(common, client)
  
  lazy val serverRun = taskKey[Unit]("Run a Firkin server.")
  
  def baseSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.freevariable",
    version := VERSION,
    resolvers ++= Seq(
      "Akka Repo" at "http://repo.akka.io/repository",
      "spray" at "http://repo.spray.io/"
    ),
    crossScalaVersions := Seq(SCALA_210_VERSION, SCALA_211_VERSION),
    licenses += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0")),
    scalacOptions ++= Seq("-feature", "-Yrepl-sync", "-target:jvm-1.7", "-Xlint")
  )
  
  def jsonSettings = Seq(
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-jackson" % JSON4S_VERSION,
      "org.json4s" %% "json4s-ext" % JSON4S_VERSION
    ) 
  )

  def colossusSettings = Seq(
    libraryDependencies ++= Seq(
      "com.tumblr" %% "colossus" % "0.5.1",
      "com.typesafe.akka" %% "akka-actor"   % AKKA_VERSION,
      "com.typesafe.akka" %% "akka-agent"   % AKKA_VERSION,
      "com.typesafe.akka" %% "akka-testkit" % AKKA_VERSION
    ) 
  )
  
  def commonSettings = baseSettings ++ colossusSettings ++ jsonSettings ++ Seq(
    name := "firkin",
    crossScalaVersions := Seq(SCALA_210_VERSION, SCALA_211_VERSION)
  )
  
  def clientSettings = baseSettings ++ jsonSettings ++ Seq(
    name := "firkin-client",
    crossScalaVersions := Seq("2.10.4", "2.11.5"),
    libraryDependencies ++= Seq(
      "net.databinder.dispatch" %% "dispatch-core" % "0.11.1"
    )
  )
  
  def serverSettings = commonSettings ++ Seq(
    initialCommands in console := """
    import com.freevariable.firkin.Firkin
    val cache = Firkin.basicStart
    """
  )
  
  val SCALA_210_VERSION = "2.10.4"
  
  val SCALA_211_VERSION = "2.11.5"
  
  val JSON4S_VERSION = "3.2.10"
  
  val AKKA_VERSION = "2.3.9"
}
