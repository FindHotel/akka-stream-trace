import scalariform.formatter.preferences._

name := "modern-scala-seed"

version := "0.1-SNAPSHOT"

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
    "org.apache.htrace" % "htrace-core4" % "4.3.0-incubating-SNAPSHOT",
    "org.apache.htrace" % "htrace-zipkin" % "4.3.0-incubating-SNAPSHOT"
  )
}

val test = Seq("org.scalactic" %% "scalactic" % "3.0.0", "org.scalatest" %% "scalatest" % "3.0.0" % "test")




fork in run := true

lazy val core = project.in( file("modules/core") )
  .settings(
    libraryDependencies ++= Seq(conf,joda)++ akka ++ trace ++ test,
    scalaV,
    resolvers += Resolver.mavenLocal
  )

