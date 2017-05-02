import scalariform.formatter.preferences._
name := "akka-stream-trace-package"

version := "0.3-SNAPSHOT"

organization := "net.findhotel"

val scalaV = scalaVersion := "2.11.8"

val joda = "joda-time" % "joda-time" % "2.8.1"

val conf = "com.typesafe" % "config" % "1.3.0"

val akka = {
  val httpV = "10.0.0"
  Seq(
    "com.typesafe.akka" %% "akka-http" % httpV,
    "com.typesafe.akka" %% "akka-http-testkit" % httpV
  )
}

val trace = {
  Seq(
    "org.apache.htrace" % "htrace-core4" % "4.3.0-SNAPSHOT",
    "org.apache.htrace" % "htrace-zipkin" % "4.3.0-SNAPSHOT"
  )
}

val test = Seq("org.scalactic" %% "scalactic" % "3.0.0", "org.scalatest" %% "scalatest" % "3.0.0" % "test")




fork in run := true

lazy val core = project.in( file("modules/core") )
  .settings(
    libraryDependencies ++= Seq(conf,joda)++ akka ++ trace ++ test,
    scalaV,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "Apache OSS Snapshots" at "https://repository.apache.org/content/groups/snapshots/")
)

lazy val examples = project.in( file("modules/examples") )
  .settings(
    libraryDependencies ++= akka,
    scalaV,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "Apache OSS Snapshots" at "https://repository.apache.org/content/groups/snapshots/")
  ).dependsOn(core)

