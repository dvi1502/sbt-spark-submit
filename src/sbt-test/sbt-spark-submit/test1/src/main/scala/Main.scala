package ru.dvi.sbt.test

import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


object Main extends Logging {

  implicit val spark: SparkSession = SparkSession.builder()
    .appName("test sbt app")
    .getOrCreate()


  val seqData = Seq(
    ("2019-01-01", 0),
    ("2019-01-12", 10),
    ("2019-01-14", 9),
    ("2019-01-15", 8),
    ("2019-01-20", 10),
    ("2019-01-25", 7),
    ("2019-01-31", 5)
  )


  def main(args: Array[String]): Unit = {
    val a = if (args.length>0) args(0) else null
    job(a)
  }

  def job(a:String): Unit = {
    val appId = spark.sparkContext.applicationId
    logInfo(s"appId = $appId")

    import spark.implicits._
    val df = spark.sparkContext.parallelize(seqData).toDF("date", "stock")
      .withColumn("value", lit(a))
    df.show()

    spark.stop()
  }


}

