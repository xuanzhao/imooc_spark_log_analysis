package spark_basic

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.hive.HiveContext

/**
  * HiveContext的使用
  * 使用时需要通过--jars 把mysql的驱动传递到classpathÒ
  */
object HiveContextApp {

  def main(args: Array[String]): Unit = {

    val warehouseLocation = "spark-warehouse"

    val sparkConf =
      new SparkConf().set("spark.sql.warehouse.dir", warehouseLocation)

    val spark =
      SparkSession.builder.enableHiveSupport.config(sparkConf).getOrCreate()

    //2)相关的处理:
    spark.table("emp").show

    //3)关闭资源
    spark.stop()
  }
}
