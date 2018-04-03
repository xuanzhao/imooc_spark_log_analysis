package spark_basic

import org.apache.spark.sql.SparkSession

/**
  * Dataset操作
  * val ds = df.as[Sales]  重点是将df[Row] 转换 df.as[Sales]
  */
object DatasetApp {

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder()
      .appName("DatasetApp")
      .master("local[2]")
      .getOrCreate()

    //注意：需要导入隐式转换
    import spark.implicits._

    val path = "file:///Users/rocky/data/sales.csv"

    //spark如何解析csv文件？
    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(path)
    df.show

    val ds = df.as[Sales]
    ds.map(line => line.itemId).show

    spark.sql("seletc name from person").show

    //df.seletc("name")
    df.select("nname")

    ds.map(line => line.itemId)

    spark.stop()
  }

  case class Sales(transactionId: Int,
                   customerId: Int,
                   itemId: Int,
                   amountPaid: Double)

}
