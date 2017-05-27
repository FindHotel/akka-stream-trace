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


val apache_oss = "Apache OSS Snapshots" at "https://repository.apache.org/content/groups/snapshots/"

fork in run := true

lazy val core = project.in( file("modules/core") )
  .settings(
    libraryDependencies ++= Seq(conf,joda)++ akka ++ trace ++ test,
    scalaV,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      apache_oss
      )
)

lazy val examples = project.in( file("modules/examples") )
  .settings(
    libraryDependencies ++= akka,
    scalaV,
    resolvers ++= Seq(
      Resolver.mavenLocal,
      apache_oss
    )
  ).dependsOn(core)


licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("http://company.findhotel.net"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/FindHotel/akka-stream-trace"),
    "scm:git@github.com:FindHotel/akka-stream-trace.git"
  )
)

developers := List(
  Developer(
    id    = "raam86",
    name  = "Raam Rosh Hai",
    email = "raam@findhotel.net",
    url   = url("https://github.com/raam86")
  )
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

