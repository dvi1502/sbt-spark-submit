package ru.dvi.sbt.test

import org.scalatest.funsuite.AnyFunSuiteLike
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


class DoProcessTest extends AnyFunSuiteLike with Logging {

  implicit val spark: SparkSession = SparkSession.builder()
    .master("local[1]")
    .appName("test sbt app")
    .getOrCreate()

  test("test") {
    Main.job("test")
  }

}
