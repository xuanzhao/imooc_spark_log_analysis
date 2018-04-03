package imooc_log_analysis

import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * 第二步：使用Spark完成我们的数据清洗操作，将数据从text转换为parquet数据
  */
object SparkStatCleanJob {

  val spark = SparkSession
    .builder()
    .appName("SparkStatCleanJob")
    .config("spark.sql.parquet.compression.codec", "gzip") // 默认是snappy
    .master("local[2]")
    .getOrCreate()

  val accessRDD =
    spark.sparkContext.textFile("/Users/rocky/data/imooc/access.log")

  accessRDD.take(10).foreach(println(_))

  // RDD ==> DF
  val accessDF = spark.createDataFrame(
    accessRDD.map(x => AccessConvertUtil.parseLog(x)),
    AccessConvertUtil.struct)

  accessDF.printSchema()
  accessDF.show(false)

  accessDF.coalesce(1).write.format("parquet").mode(SaveMode.Overwrite).partitionBy("day").save("/Users/rocky/data/imooc/clean2")

  spark.stop()
}
