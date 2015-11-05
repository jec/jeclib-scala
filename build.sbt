name := "jeclib"

organization := "net.jcain"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion       = "2.4.0"
  Seq(
    "ch.qos.logback"    % "logback-classic" % "1.1.3",
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit"   % akkaVersion   % "test",
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
  )
}
