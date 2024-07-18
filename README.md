# sbt-spark-submit


## Зачем нужен

Отправляйте задания Spark с помощью конфигураций запуска в вашей sbt.
Отслеживайте задания Spark из консоли sbt.
Является альтернативой инструмента Big Data Tools от JetBrains.
Плагин позволяет подключаться к удаленным хранилищам sftp.
Плагин может запускать приложение кластере спарк через ssh.

Этот плагин не является частью пакета плагинов Big Data Tools.
This plugin not is a part of the Big Data Tools plugin bundle.

## Как подключить

1. сделать локальный репозиторий sbt-spark-submit проекта командой
```bash
$> git clone https://github.com/dvi1502/sbt-spark-submit.git
```

2. собрать и опубликовать sbt-spark-submit проект в локальном репозитории
```bash
$> sbt compile  publishLocal
```

3. В своем проекте (например my-first-spark-project) подключить sbt-плагин, добавив строки в файл project/plugins.sbt  

```sbt
resolvers +=  Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns)

addSbtPlugin("ru.dvi" % "sbt-spark-submit" % "0.1.0-SNAPSHOT")  
```

4. Добавить настройки в build.sbt своего проекта my-first-spark-project

```sbt

 lazy val sparkSubmitConf = Seq(
   ???
 )
 
 lazy val root = (project in file("."))
   .settings(
     name := "my-first-spark-project"
   )
   .settings(sparkSubmitConf)

```

Описание настроек плагина приведено в следующем разделе.


## Как применять на практике

### Настройки (settings) для подключения к ssh (sftp) - remote target

* sshUser  - имя пользователя для ssh-сервера   
* sshHost  - url ssh-сервера  
* sshPort  - Порт ssh-сервера (default 22)  
* sshKey   - private key file   
* workingDirectory   - target upload directory  
* beforeSubmitScript - before submit script  
* afterSubmitScript  - after submit script
* scriptEnvironments - задать переменные окружения, выполняется перед выполнением каждой команды beforeSubmitScript и afterSubmitScript

**Пример:**  

```sbt
lazy val sparkSubmitConf = Seq(
  sshUser := "dvi", // your username on host
  sshHost := "client.dmp.dvi.ru",
  sshKey := "~/.ssh/id_dmp", // path to private key in OpenSSH format
  workingDirectory := "/home/dvi/my-first-spark-project",

  scriptEnvironments := Map(
    "SPARK_MAJOR_VERSION" -> "3",
  ),

  beforeSubmitScript := Seq(
    "echo $SPARK_MAJOR_VERSION",
    "ls -la " + (state / workingDirectory).value,
  ),

  afterSubmitScript := Seq(
    "echo $SPARK_APP_ID",
    "yarn logs -applicationId $SPARK_APP_ID -out " + (state / workingDirectory).value + "/yarn-log.txt",
  )
)
```

### Run configuration

* sparkDeployMode  - как следует запускать программу драйвера "локально" («клиент») или на одной из рабочих машин внутри кластера («кластер») (по умолчанию: клиент).  
* sparkMaster  - spark://host:port, mesos://host:port, yarn, k8s://https://host:port, or local (Default: local[*]). [cluster manager](https://spark.apache.org/docs/latest/cluster-overview.html)
* sparkDriverMemory - memory for driver (e.g. 1000M, 2G) (Default: 1024M).
* sparkExecutorMemory -  memory per executor (e.g. 1000M, 2G) (Default: 1G).
* className - основной класс вашего приложения (for Java / Scala apps).
* sparkName - название вашего приложения.
* sparkFiles - разделенный запятыми список файлов, которые необходимо поместить в рабочий каталог каждого executor. 
* sparkJars - список jar-файлов, разделенных запятыми, для включения в пути к классам driver и executor.
* sparkEnableDependencies - если присвоено значение TRUE (Default: FALSE)  будут добавлены опции:  
  
    + --repositories - cписок дополнительных удаленных репозиториев, разделенных запятыми, для поиска координат maven. 
    + Значения будут взяты из настройки **resolvers** build.sbt текущего проекта.  
  
    + --packages - разделенный запятыми список maven-координат jar-файлов для включения в classpath драйвера и исполнителя. 
 Будет выполнен поиск в локальном репозитории maven, затем в maven central и любых дополнительных удаленных репозиториях, указанных параметром --repositories. 
Формат координат должен быть groupId:artifactId:version. Значения будут взяты из настройки **libraryDependencies** build.sbt текущего проекта.  
* sparkArgs - список аргументов при запуске приложения
* sparkVerbose - распечатает дополнительный отладочный вывод. (Default: FALSE)
* sparkConf - произвольное свойство конфигурации Spark.


**Пример :**

```sbt
lazy val sparkSubmitConf = Seq(

  // sparkDeployMode := Client,
  // sparkMaster := Local(numWorkers = Some(2), maxFailures = None)

  sparkDeployMode := Cluster(driverCores = Some(2)),
  sparkMaster := Yarn(
    numExecutors = Some(2),
    executorCores = Some(2),
    //    principal = Some(s"${(state / sshUser).value}"),
    //    keytab = Some(s"${(state / workingDirectory).value}/${(state / sshUser).value}.keytab".toLowerCase())
  ),

  sparkDriverMemory := Some("512m"),
  sparkExecutorMemory := Some("512m"),
  className := "ru.dvi.sbt.test.Main",
  sparkName := "My tested application",
  sparkVerbose := false,
  sparkEnableDependencies := false,

  sparkConf := Map(
    "spark.executor.extraJavaOptions" -> "-Djava.security.auth.login.config=./jaas.conf -Dconfig.file=./application.conf ",
    "spark.driver.extraJavaOptions" -> "-Djava.security.auth.login.config=./jaas.conf -Dconfig.file=./application.conf ",
  ),

  sparkArgs := Seq(
    "my arg first",
    "my arg 2",
  ),

  sparkFiles := Seq(
    "configs/application.conf",
    "configs/jaas.conf",
  ),

  sparkJars := Seq(
    "configs/spark-sql-kafka-0-10_2.12-3.0.1.jar",
    "configs/spark-streaming-kafka-0-10_2.12-3.0.1.jar",
  )
)
```

## Как использовать

Этот плагин sperk-submit целесообразно использовать совместно с плагином [sbt-assembly](https://github.com/sbt/sbt-assembly) . 
Для этого в файл plugins.sbt нужно добавить строчку:
```sbt
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
```

После того как настройки сделаны плагин sbt-spark-submit готов к использованию. 


```bash
$> sbt assembly
$> sbt sparkSubmit
```

