name := """logging-engine"""

version := "1.0"

scalaVersion := "2.11.2"

crossScalaVersions  := Seq("2.11.2", "2.11.1", "2.10.4")

val commonlibraryDependencies = Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test" withSources,
  //"com.typesafe.scala-logging" %% "scala-logging" % "3.0.0" withSources,
  //"org.clapper" %% "grizzled-slf4j" % "1.0.2" withSources(),
  //"org.scalaz" %% "scalaz-core" % "7.0.6" withSources
  "com.typesafe" % "config" % "1.2.1" withSources
)

val loglibraryDependencies = commonlibraryDependencies ++ Seq(
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0" withSources,
  "com.typesafe.play"       %% "play-json"                   % "2.3.2" withSources,
  "com.typesafe.akka" %% "akka-actor" % "2.3.4" withSources,
  "com.typesafe.akka" %% "akka-remote" % "2.3.4" withSources
)

seq(bintrayResolverSettings:_*)

lazy val logModels = (project in file("log-models")).settings({
  version := "1.0"
  scalaVersion := "2.11.2"
  crossScalaVersions  := Seq("2.11.2", "2.11.1", "2.10.4")
  libraryDependencies ++= loglibraryDependencies
})

//lazy val root = (project in file(".")).settings(libraryDependencies ++= loglibraryDependencies).dependsOn(logModels).aggregate(logModels)
lazy val root = (project in file(".")).settings(libraryDependencies ++= loglibraryDependencies).aggregate(logModels)

