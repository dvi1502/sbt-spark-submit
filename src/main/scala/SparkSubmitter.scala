package ru.dvi.sbt.sparksubmit


import sbt.*

import java.nio.file.FileSystems
import scala.collection.mutable.ArrayBuffer
import scala.sys.process.*


class SparkSubmitter(settings: SparkSubmitSettings, sshSettings: SSHSettings, logger: Logger) {

  val environments = scala.collection.mutable.ArrayBuffer[String]()

  def commandLine(app: String, jars: Seq[String], files: Seq[String]): Seq[String] = {

    val sparkSbmitCmd = (settings.master, System.getProperty("os.name").contains("Windows")) match {
      case (_: Local, true) => "spark-submit.cmd"
      case (_, _) => "spark-submit"
    }

    val cmd = ArrayBuffer(sparkSbmitCmd, "--class", settings.mainClass)

    cmd ++= settings.master.args

    cmd ++= settings.deployMode.args

    if (settings.appName.nonEmpty) cmd ++= Seq("--name", s""""${settings.appName}"""")
    if (settings.driverMemory.nonEmpty) cmd ++= Seq("--driver-memory", s""""${settings.driverMemory.get}"""")

    if (settings.driverLibraryPath.nonEmpty) cmd ++= Seq("--driver-library-path", s""""${settings.driverLibraryPath.get}"""")
    if (settings.driverClassPath.nonEmpty) cmd ++= Seq("--driver-class-path", s""""${settings.driverClassPath.get}"""")

    if (settings.executorMemory.nonEmpty) cmd ++= Seq("--executor-memory", s""""${settings.executorMemory.get}"""")
    if (settings.proxyUser.nonEmpty) cmd ++= Seq("--proxy-user", s""""${settings.proxyUser.get}"""")

    if (settings.verbose) cmd ++= Seq("--verbose")

    if (files.mkString(",").nonEmpty) cmd ++= Seq("--files", s""""${files.mkString(",")}"""")
    if (jars.mkString(",").nonEmpty) cmd ++= Seq("--jars", s""""${jars.mkString(",")}"""")


    logger.info("********* packages ***********")
    settings.packages.foreach { pkg =>
      logger.info(pkg.toString())
      logger.info(s"org=${pkg.organization}, name=${pkg.name}, revision=${pkg.revision}, crossVersion=${pkg.crossVersion}")
    }

    val packageArgs = finalDependencies.map(_.toString)

    if (packageArgs.nonEmpty) cmd ++= Seq("--packages", s""""${packageArgs.mkString(",")}"""")


    val confArgs = settings.conf.flatMap { case (key, value) => Seq("--conf", s""""$key=$value"""") }

    cmd ++= confArgs

    if (settings.resolvers.nonEmpty) {
      cmd ++= Seq("--repositories", s""""${settings.resolvers.mkString(",")}"""")
    }

    cmd += app

    cmd ++= settings.args.map { arg => s""""$arg"""" }

    cmd
  }

  def copy(files: Seq[String]): Seq[String] =

    settings.master match {

      case _: Local =>
        import java.io.File
        files.map { filename =>
          val fl = s"${new File("").getAbsolutePath()}/${filename}"
          logger.debug(fl)
          fl
        }

      case _ =>
        files.map { filename =>
          SFTP.upload(sshSettings, filename, logger)
          val file = FileSystems.getDefault.getPath(filename)
          val fl = s"${sshSettings.SFTPDSTDIR}/${file.getFileName.toString}"
          logger.debug(fl)
          fl
        }

    }

  def run(commands: Seq[String]): Unit = {

    (settings.master, commands.nonEmpty) match {
      case (_: Local, true) =>
        commands.foreach { command =>
          if (s"$command".! != 0) throw new RuntimeException("Error while running application")
        }
      case (_, true) =>
        commands.foreach { command =>
          SSH.exec(sshSettings, (environments.toSeq :+ command) .mkString(";"), logger) match {
            case Some(value) => environments += s"""export SPARK_APP_ID=${value}"""
            case None =>
          }
        }
      case (_, false) =>
        logger.info("Script not defined")
    }
  }

  def submit(): Unit = {
    logger.info("**** config ********************")
    logger.info(settings.pretty())

    logger.info("********************************")
    logger.info(sshSettings.pretty())

    logger.info("")
    logger.info("**** before script *************")
    settings.scriptEnvironments.flatMap { case (key, value) => environments += s"""export $key=$value""" }
    run(settings.beforeScript)


    logger.info("**** files *********************")
    val app = copy(Seq(settings.appJar.toString))
    val jars = copy(settings.jars)
    val files = copy(settings.files)

    val command = commandLine(app(0), jars, files)

    logger.info("**** spark-submit **************")
    run(Seq(command.mkString(" ")))

    logger.info("**** after script **************")
    run(settings.afterScript)

  }

  def finalDependencies = {
    val crossVersion = CrossVersion(settings.scalaVersion, CrossVersion.binaryScalaVersion(settings.scalaVersion))
    settings.packages
      .filterNot(_.configurations.contains("provided"))
      .filterNot(_.configurations.contains("test"))
      .filterNot(_.configurations.contains("compile"))
      .map(crossVersion)
  }

}
