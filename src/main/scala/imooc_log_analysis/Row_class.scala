package imooc_log_analysis

case class DayVideoTrafficsStat(day: String, cmsId: Long, traffics: Long)

/**
  * 每天课程访问次数实体类
  */
case class DayVideoAccessStat(day: String, cmsId: Long, times: Long)

case class DayCityVideoAccessStat(day: String,
                                  cmsId: Long,
                                  city: String,
                                  times: Long,
                                  timesRank: Int)
