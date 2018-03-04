name := "akka-http-in-scala-ecosystem"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"              % "10.1.0-RC2",
  "com.typesafe.akka" %% "akka-http-spray-json"   % "10.1.0-RC2",
  "com.typesafe.akka" %% "akka-http-xml"          % "10.1.0-RC2",
  "com.typesafe.akka" %% "akka-stream"            % "2.5.9",
  "com.typesafe.akka" %% "akka-http-testkit"      % "10.1.0-RC2"    % Test,
  "org.scalatest"     %% "scalatest"              % "3.0.5"         % Test
)