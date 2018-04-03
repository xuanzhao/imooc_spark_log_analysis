name := "imooc_spark_log_analysis"

version := "0.1"

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.0",

  "org.apache.spark" %% "spark-streaming" % "2.2.0",

  "org.apache.spark" %% "spark-sql" % "2.2.0",

  "org.apache.spark" %% "spark-hive" % "2.2.0",

  "org.apache.spark" %% "spark-graphx" % "2.2.0"
)

libraryDependencies ++= Seq(
  "javax.servlet" % "servlet-api" % "2.5",
  "javax.servlet" % "jsp-api" % "2.0"
)