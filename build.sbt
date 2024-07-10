import sbt._
import sbt.plugins._
import sbt.ScriptedPlugin.autoImport._



ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.19"

ThisBuild / organization := "ru.dvi"

val ivyLocal = Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

libraryDependencies += "com.jcraft" % "jsch" % "0.1.55"
//libraryDependencies += "com.github.mwiede" % "jsch" % "0.2.18" // fork jcraft https://github.com/mwiede/jsch



lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-spark-submit",
    publishTo := Some(ivyLocal),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.4.7" // set minimum sbt version
      }
    },

  )


