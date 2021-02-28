name := "orderdeliveryhandler"
version := "0.1"
scalacOptions += "-Ypartial-unification"
scalaVersion := "2.12.6"
val circeVersion = "0.10.0"
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.typelevel" %% "cats-core" % "1.4.0",
  "com.typesafe.akka" %% "akka-actor" % "2.5.17",
  "com.typesafe.akka" %% "akka-stream" % "2.5.17",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.17" % Test,
  "com.typesafe.akka" %% "akka-http" % "10.1.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.5" % Test,
  "de.heikoseeberger" %% "akka-http-circe" % "1.22.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val sparkVersion = "3.0.0-preview2"
val appDependencies = Seq(
  "com.datastax.spark" %% "spark-cassandra-connector" % "3.0.0-44"
)
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion
)
// https://mvnrepository.com/artifact/com.fasterxml.uuid/java-uuid-generator
libraryDependencies += "com.fasterxml.uuid" % "java-uuid-generator" % "3.1.0"
libraryDependencies += "log4j" % "log4j" % "1.2.14"
// https://mvnrepository.com/artifact/com.github.jnr/jnr-posix
libraryDependencies += "com.github.jnr" % "jnr-posix" % "3.0.61"
// https://mvnrepository.com/artifact/joda-time/joda-time
libraryDependencies += "joda-time" % "joda-time" % "2.10.6"



dockerBaseImage := "openjdk:8-jre-alpine"
dockerExposedPorts := Seq(8080)

enablePlugins(AshScriptPlugin, JavaAppPackaging, DockerPlugin)