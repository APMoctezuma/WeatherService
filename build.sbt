ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.3"

val AkkaVersion = "2.8.5"
val AkkaHttpVersion = "10.5.3"

lazy val root = (project in file("."))
  .settings(
    name := "WeatherService",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
      "org.scala-lang" %% "toolkit" % "0.9.1",
      // Cucumber - Scala
      "io.cucumber" %% "cucumber-scala" % "8.39.1"  % Test,
      "io.cucumber" % "cucumber-junit-platform-engine" % "7.34.3" % Test,
      // JUnit 5 Platform
      "org.junit.platform" % "junit-platform-suite" % "1.11.0" % Test,
      "org.junit.jupiter" % "junit-jupiter-api" % "5.11.0" % Test,
      "org.junit.jupiter" % "junit-jupiter-engine" % "5.11.0" % Test,
      // Mockito
      "org.mockito" % "mockito-core" % "5.23.0" % Test
    )
  )
