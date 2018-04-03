package spark_basic

import org.apache.spark.sql.SparkSession

/**
  * SparkSession的使用
  *
  * SparkSession是spark2.0以后默认的的统一客户端程序入口。
  *
  * sparkSession是HiveContext和sqlContext的统一入口
  * sparkContext可以通过spark.sparkContext获得
  */
object SparkSessionApp {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder()
      .enableHiveSupport()
      .appName("SparkSessionApp")
      .master("local[2]")
      .getOrCreate()
    val sc = spark.sparkContext

    val people = spark.read.json("file:///Users/rocky/data/people.json")
    people.show()

    spark.stop()
  }
}
