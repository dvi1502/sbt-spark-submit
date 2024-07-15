package ru.dvi.sbt.sparksubmit

import ru.dvi.sbt.sparksubmit.Extensions._

sealed trait Master {
  def args: Seq[String]
}

case class Spark(urls: Seq[String], totalExecutorCores: Option[Int] = None, supervise: Boolean = false) extends Master {
  def args: Seq[String] = {
    val base = Seq(
      ("--master", Some(s"spark://${urls.mkString(",")}")),
      ("--total-executor-cores", totalExecutorCores)
    ).toArgs

    if (supervise) base :+ "--supervise" else base
  }
}

case class Mesos(url: String, totalExecutorCores: Option[Int] = None, zooKeeper: Boolean = false, supervise: Boolean = false) extends Master {
  def args: Seq[String] = {
    val base = Seq(
      ("--master", Some(if (zooKeeper) s"mesos://zk://$url" else s"mesos://$url")),
      ("--total-executor-cores", totalExecutorCores)
    ).toArgs

    if (supervise) base :+ "--supervise" else base
  }
}

case class Yarn(numExecutors: Option[Int] = None,
                executorCores: Option[Int] = None,
                queue: Option[String] = None,
                archives: Seq[String] = Nil,
                principal: Option[String] = None,
                keytab: Option[String] = None,
               ) extends Master {

  def args: Seq[String] = {
    val cmd = Seq(
      ("--master", Some("yarn")),
      ("--num-executors", numExecutors),
      ("--executor-cores", executorCores),
      ("--queue", queue),
      ("--principal", principal),
      ("--keytab", keytab),
    ).toArgs

    if (archives.nonEmpty) cmd ++ Seq("--archives", archives mkString ",") else cmd
  }
}

case class K8s(url: String) extends Master {
  def args = Seq("--master", s"k8s://$url")
}

case class Local(numWorkers: Option[Int] = None, maxFailures: Option[Int] = None) extends Master {
  def args: Seq[String] = {
    val master = (numWorkers, maxFailures) match {
      case (Some(nw), Some(mf)) => s"local[$nw:$mf]"
      case (Some(nw), None) => s"local[$nw]"
      case (None, Some(mf)) => s"local[*:$mf]"
      case (None, None) => s"local[*]"
    }

    Seq("--master", master)
  }
}

sealed trait DeployMode {
  def args: Seq[String]
}

case class Cluster(driverCores: Option[Int] = None) extends DeployMode {
  def args = Seq(
    ("--deploy-mode", Some("cluster")),
    ("--driver-cores", driverCores)
  ).toArgs
}

case object Client extends DeployMode {
  def args = Seq("--deploy-mode", "client")
}
