package ru.dvi.sbt.sparksubmit

import sbt.{File, ModuleID, Resolver}

case class SparkSubmitSettings(
                                appJar: File,
                                mainClass: String,
                                args: Seq[String],
                                master: Master,
                                deployMode: DeployMode,
                                appName: String,
                                jars: Seq[String],
                                files: Seq[String],
                                conf: Map[String, Any],
                                driverMemory: Option[String],
                                driverJavaOptions: Option[String],
                                driverLibraryPath: Option[String],
                                driverClassPath: Option[String],
                                executorMemory: Option[String],
                                proxyUser: Option[String],
                                verbose: Boolean,
                                enableDependencies: Boolean,
                                packages: Seq[ModuleID],
                                resolvers: Seq[Resolver],
                                scalaVersion: String,
                                beforeScript: Seq[String],
                                afterScript: Seq[String],
                                scriptEnvironments: Map[String,String],
                              )
{
  def pretty(): String =
    classOf[SparkSubmitSettings]
      .getDeclaredFields
      .map { f =>
        f.setAccessible(true)
        val res = (f.getName, f.get(this))
        f.setAccessible(false)
        s"${res._1.padTo(25, ' ')}\t:\t${res._2}"
      }.mkString("\n") + "\n---\n"
}