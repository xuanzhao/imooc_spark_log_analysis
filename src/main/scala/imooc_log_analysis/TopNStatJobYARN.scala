package imooc_log_analysis

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

import scala.collection.mutable.ListBuffer


/**
  * TopN统计Spark作业:运行在YARN之上
  */
object TopNStatJobYARN {

  def main(args: Array[String]) {

    if(args.length !=2) {
      println("Usage: TopNStatJobYARN <inputPath> <day>")
      System.exit(1)
    }

    val Array(inputPath, day) = args
    val spark = SparkSession.builder()
      .config("spark.sql.sources.partitionColumnTypeInference.enabled","false")
      .getOrCreate()


    val accessDF = spark.read.format("parquet").load(inputPath)

    StatDAO.deleteData(day)

    //最受欢迎的TopN课程
    videoAccessTopNStat(spark, accessDF, day)

    //按照地市进行统计TopN课程
    cityAccessTopNStat(spark, accessDF, day)

    //按照流量进行统计
    videoTrafficsTopNStat(spark, accessDF, day)

    spark.stop()
  }

  /**
    * 按照流量进行统计
    */
  def videoTrafficsTopNStat(spark: SparkSession, accessDF:DataFrame, day:String): Unit = {
    import spark.implicits._

    val cityAccessTopNDF = accessDF.filter($"day" === day && $"cmsType" === "video")
      .groupBy("day","cmsId").agg(sum("traffic").as("traffics"))
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
  def cityAccessTopNStat(spark: SparkSession, accessDF:DataFrame, day:String): Unit = {
    import spark.implicits._

    val cityAccessTopNDF = accessDF.filter($"day" === day && $"cmsType" === "video")
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


  /**
    * 最受欢迎的TopN课程
    */
  def videoAccessTopNStat(spark: SparkSession, accessDF:DataFrame, day:String): Unit = {

    /**
      * 使用DataFrame的方式进行统计
      */
    import spark.implicits._

    val videoAccessTopNDF = accessDF.filter($"day" === day && $"cmsType" === "video")
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

          list.append(DayVideoAccessStat(day, cmsId, times))
        })

        StatDAO.insertDayVideoAccessTopN(list)
      })
    } catch {
      case e:Exception => e.printStackTrace()
    }

  }

}
