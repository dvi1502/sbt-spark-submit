import sbt.Keys._
import ru.dvi.sbt.sparksubmit._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / organization := "ru.dvi"

ThisBuild / scalaVersion := "2.12.19"


val sparkVersion = "3.0.1"


resolvers ++= Seq(
  "Maven Central Server" at "https://repo1.maven.org/maven2",
)


scalacOptions += "-Ypartial-unification"


lazy val sparkSubmitConf = Seq(

  sshHost := "client.dmp.dvi.ru", // your host address
  sshKey := "~/.ssh/id_dmp", // path to private key in OpenSSH format
  workingDirectory := "/home/dvi/sbt", // dest directory of jar file

  beforeSubmitScript := Seq(
    "export SPARK_MAJOR_VERSION=3",
    "rm -r /home/dvi/sbt/*"
  ),

  //  sparkMaster := Local(),
  className := "ru.dvi.sbt.test.Main",
  sparkName := "My tested application",
  sparkVerbose := false,

  sparkConf := Map(
    "spark.executor.memory" -> "512m",
    "spark.executor.memoryOverhead" -> "512m",
  ),

  sparkArgs := Seq(
    "yarn running",
    "local running",
  ),

  sparkFiles := Seq(
    "src/main/resources/application.conf",
    "src/main/resources/app1.conf",
  ),

  sparkJars := Seq(
    "src/main/resources/spark-sql-kafka-0-10_2.12-3.0.1.jar",
    "src/main/resources/spark-streaming-kafka-0-10_2.12-3.0.1.jar"
  ),

)


lazy val root = (project in file(".")).
  settings(
    scalaVersion := "2.12.19",
    sbtVersion := "1.7.1",
  )
  .settings(sparkSubmitConf)


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.scalatest" %% "scalatest-funsuite" % "3.2.18" % Test,
)


assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
