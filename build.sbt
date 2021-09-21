name := "jeclib"

organization := "net.jcain"

version := "0.2.0-SNAPSHOT"

scalaVersion := "2.13.6"

libraryDependencies ++= {
  val akkaVersion = "2.6.16"
  Seq(
    "ch.qos.logback"    % "logback-classic" % "1.2.6",
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit"   % akkaVersion   % "test",
    "org.scalatest"     %% "scalatest"      % "3.2.9"       % "test"
  )
}
