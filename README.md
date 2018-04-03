[慕课网实战 以慕课网日志分析为例 进入大数据 Spark SQL 的世界](https://coding.imooc.com/class/112.html)

课程笔记目录是`note`

课程spark相关实战代码目录是`src\main\scala`

课程可视化web服务实战代码目录是`SparkWeb`

SparkWeb目录是用java tomcat搭建的数据可视化web服务，将spark处理并保存到MySQL的数据抽取并调用Echarts js库进行可视化展现。如果不是java出身，我们可以使用很多其他语言或者脚本搭建这个数据可视化web服务。


[项目提供的IP地址解析依赖包git地址](https://github.com/wzhe06/ipdatabase)

下载下来后进入该项目根目录，将其打包并安装到本地maven仓库（也可以使用sbt工具进行打包和安装到本地sbt仓库）：

`mvn clean package -DskipTests`

`mvn install:install-files path -DgroupId=com.ggstar -DartifactId=ipdatabases -Dversion=1.0 -Dversion=1.0 -Dpackaging=jar`

由于这个ip地址解析工具有自己依赖的数据库(在resources目录下），更好的做法是将该依赖与整个项目进行集成打包：

`maven:assembly assembly`

项目在集群上提交的时候要记得把ip地址解析依赖的数据一起提交：

```
./bin/spark-submit \
--class com.imooc.log.SparkStatCleanJobYARN \
--name SparkStatCleanJobYARN \
--master yarn \
--executor-memory 1G \
--num-executors 1 \
--files /home/hadoop/lib/ipDatabase.csv,/home/hadoop/lib/ipRegion.xlsx \
/home/hadoop/lib/sql-1.0-jar-with-dependencies.jar \
hdfs://hadoop001:8020/imooc/input/* hdfs://hadoop001:8020/imooc/clean
```


