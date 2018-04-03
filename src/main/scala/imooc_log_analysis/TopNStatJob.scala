package imooc_log_analysis

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

import scala.collection.mutable.ListBuffer


/**
  * 第三步：TopN统计Spark作业：复用已有的数据
  */
object TopNStatJob {



  def main(args: Array[String]) {

    val spark = SparkSession.builder().appName("TopNStatJob")
      .config("spark.sql.sources.partitionColumnTypeInference.enabled","false")
      .master("local[2]").getOrCreate()


    val accessDF = spark.read.format("parquet").load("/Users/rocky/data/imooc/clean")

    accessDF.printSchema()
    accessDF.show(false)

    val day = "20170511"

    import spark.implicits._
    val commonDF = accessDF.filter($"day" === day && $"cmsType" === "video")

    commonDF.cache() // 将可复用的数据缓存到内存中，可以在sparkUI的storage页面看到相关信息。

    StatDAO.deleteData(day)

    //最受欢迎的TopN课程
    videoAccessTopNStat(spark, commonDF)

    //按照地市进行统计TopN课程
    cityAccessTopNStat(spark, commonDF)

    //按照流量进行统计
    videoTrafficsTopNStat(spark, commonDF)

    commonDF.unpersist(true)

    spark.stop()
  }

  /**
    * 最受欢迎的TopN课程
    */
  def videoAccessTopNStat(spark: SparkSession, commonDF:DataFrame): Unit = {

    /**
      * 使用DataFrame的方式进行统计
      */
    import spark.implicits._

    val videoAccessTopNDF = commonDF
      .groupBy("day","cmsId").agg(count("cmsId").as("times")).orderBy($"times".desc)

    videoAccessTopNDF.show(false)

    /**
      * 使用SQL的方式进行统计
      */
    //    accessDF.createOrReplaceTempView("access_logs")
    //    val videoAccessTopNDF = spark.sql("select day,cmsId, count(1) as times from access_logs " +
    //      "where day='20170511' and cmsType='video' " +
    //      "group by day,cmsId order by times desc")
    //
    //    videoAccessTopNDF.show(false)

    /**
      * 将统计结果写入到MySQL中
      */
    try {
      videoAccessTopNDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoAccessStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val times = info.getAs[Long]("times")

          /**
            * 不建议大家在此处进行数据库的数据插入
            */

          list.append(DayVideoAccessStat(day, cmsId, times))
        })

        StatDAO.insertDayVideoAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }

  /**
    * 按照流量进行统计
    */
  def videoTrafficsTopNStat(spark: SparkSession, commonDF:DataFrame): Unit = {
    import spark.implicits._

    val cityAccessTopNDF = commonDF.groupBy("day","cmsId")
      .agg(sum("traffic").as("traffics"))
      .orderBy($"traffics".desc)
    //.show(false)

    /**
      * 将统计结果写入到MySQL中
      */
    try {
      cityAccessTopNDF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayVideoTrafficsStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val traffics = info.getAs[Long]("traffics")
          list.append(DayVideoTrafficsStat(day, cmsId,traffics))
        })

        StatDAO.insertDayVideoTrafficsAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }


  /**
    * 按照地市进行统计TopN课程
    */
  def cityAccessTopNStat(spark: SparkSession, commonDF:DataFrame): Unit = {



    val cityAccessTopNDF = commonDF
      .groupBy("day","city","cmsId")
      .agg(count("cmsId").as("times"))

    //cityAccessTopNDF.show(false)

    //Window函数在Spark SQL的使用

    val top3DF = cityAccessTopNDF.select(
      cityAccessTopNDF("day"),
      cityAccessTopNDF("city"),
      cityAccessTopNDF("cmsId"),
      cityAccessTopNDF("times"),
      row_number().over(Window.partitionBy(cityAccessTopNDF("city"))
        .orderBy(cityAccessTopNDF("times").desc)
      ).as("times_rank")
    ).filter("times_rank <=3") //.show(false)  //Top3


    /**
      * 将统计结果写入到MySQL中
      */
    try {
      top3DF.foreachPartition(partitionOfRecords => {
        val list = new ListBuffer[DayCityVideoAccessStat]

        partitionOfRecords.foreach(info => {
          val day = info.getAs[String]("day")
          val cmsId = info.getAs[Long]("cmsId")
          val city = info.getAs[String]("city")
          val times = info.getAs[Long]("times")
          val timesRank = info.getAs[Int]("times_rank")
          list.append(DayCityVideoAccessStat(day, cmsId, city, times, timesRank))
        })

        StatDAO.insertDayCityVideoAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }

}
