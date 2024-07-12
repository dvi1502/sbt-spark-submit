package ru.dvi.sbt.sparksubmit

import sbt.Keys.*
import sbt.plugins.JvmPlugin
import sbt.{AutoPlugin, Command, Compile, Def, File, Keys, Project, Resolver, ThisBuild, settingKey, taskKey}

// TODO: добавить проверку на размер, хеш, дату создания файлов перед тем как их копировать. Пропускать при копировании если уже есть такие на sftp.

object SparkSubmitPlugin extends AutoPlugin {
  override def trigger = allRequirements

  override def requires = JvmPlugin

  object autoImport {

    // App settings
    val sparkArgs = settingKey[Seq[String]]("Application arguments")
    val className = settingKey[String]("Default runner ClassName")


    // Spark submit settings
    val sparkMaster = settingKey[Master]("Spark master")
    val sparkDeployMode = settingKey[DeployMode]("Whether to launch the driver program locally ('client') or on one of the worker machines inside the cluster ('cluster')")
    val sparkName = settingKey[String]("A name of your application")
    val sparkJars = settingKey[Seq[String]]("List of jars to include on the driver and executor classpaths")
    // val sparkExcludePackages = settingKey[Seq[ModuleID]]("List of excluded packages")
    val sparkRepositories = settingKey[Seq[Resolver]]("Additional resolvers")
    val sparkFiles = settingKey[Seq[String]]("List of files to be placed in the working directory of each executor")
    val sparkConf = settingKey[Map[String, Any]]("Arbitrary Spark configuration property")
    val sparkDriverMemory = settingKey[Option[Long]]("Memory for driver in bytes")
    val sparkDriverJavaOptions = settingKey[Option[String]]("Extra Java options to pass to the driver")
    val sparkDriverLibraryPath = settingKey[Option[String]]("Extra library path entries to pass to the driver")
    val sparkDriverClassPath = settingKey[Option[String]]("Extra class path entries to pass to the driver")
    val sparkExecutorMemory = settingKey[Option[Long]]("Memory per executor")
    val sparkProxyUser = settingKey[Option[String]]("User to impersonate when submitting the application.")
    val sparkVerbose = settingKey[Boolean]("Print additional debug output")


    // SSH configuration
    val sshHost = settingKey[String]("SSH host")
    val sshUser = settingKey[String]("SSH user")
    val sshPort = settingKey[Int]("SSH port")
    val sshKey = settingKey[String]("SSH key")
    //    val sshPassword = settingKey[String]("SSH password")


    // Shell options
    val workingDirectory = settingKey[String]("Point to the script call location.")
    val scriptEnvironments = settingKey[Map[String,String]]("SSH key")
    val beforeSubmitScript = settingKey[Seq[String]]("Script witch be executed before spark submit.")
    val afterSubmitScript = settingKey[Seq[String]]("Script witch be executed after spark submit.")

    val sparkSubmit = taskKey[Unit]("Submit Spark application")

  }

  import autoImport.*


  lazy val sparkSubmitTask = Def.task {

    val mainClass = (state / className).value

    val appJar = new Jar(
      (state / name).value,
      (state / version).value,
      (state / scalaVersion).value
    ).file

    val sshSettings = SSHSettings(
      (state / sshHost).value,
      (state / sshPort).value,
      (state / sshUser).value,
      new KeyAuth((state / sshKey).value),
      (state / workingDirectory).value,
      "",
    )


    val settings = SparkSubmitSettings(
      appJar = appJar,
      mainClass = mainClass,
      args = (state / sparkArgs).value,
      master = (state / sparkMaster).value,
      deployMode = (state / sparkDeployMode).value,
      appName = (state / sparkName).value,
      jars = (state / sparkJars).value,
      files = (state / sparkFiles).value,
      conf = (state / sparkConf).value,
      driverMemory = (state / sparkDriverMemory).value,
      driverJavaOptions = (state / sparkDriverJavaOptions).value,
      driverLibraryPath = (state / sparkDriverLibraryPath).value,
      driverClassPath = (state / sparkDriverLibraryPath).value,
      executorMemory = (state / sparkExecutorMemory).value,
      proxyUser = (state / sparkProxyUser).value,
      verbose = (state / sparkVerbose).value,
      packages = libraryDependencies.value,
      scalaVersion = scalaVersion.value,
      resolvers = (state / sparkRepositories).value,
      beforeScript = (state / beforeSubmitScript).value,
      afterScript = (state / afterSubmitScript).value,
      scriptEnvironments = (state / scriptEnvironments).value,
    )

    val submit = new SparkSubmitter(settings, sshSettings, streams.value.log)

    submit.submit()

  }


  override lazy val projectSettings = Seq(
    sshHost := "127.0.0.1",
    sshUser := sys.props("user.name"),
    sshPort := 22,
    sshKey := "~/.ssh/id_dmp",
    workingDirectory := s"/home/${sshUser}",

    scriptEnvironments := Map.empty[String, String],
    beforeSubmitScript := Nil,
    afterSubmitScript := Nil,

    className := "Main",
    sparkArgs := Nil,

    sparkMaster := Yarn(),
    sparkDeployMode := Client,
    sparkName := name.value,
    sparkJars := Nil,
    sparkFiles := Nil,
    sparkConf := Map.empty[String, Any],
    sparkDriverMemory := None,
    sparkDriverJavaOptions := None,
    sparkDriverLibraryPath := None,
    sparkDriverClassPath := None,
    sparkExecutorMemory := None,
    sparkProxyUser := None,
    sparkVerbose := false,
    sparkRepositories := resolvers.value,


    sparkSubmit := sparkSubmitTask.value,

  )

  override lazy val buildSettings = Seq()

  override lazy val globalSettings = Seq()


}
