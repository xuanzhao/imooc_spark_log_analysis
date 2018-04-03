package imooc_log_analysis

object IpHelper {
  def findRegionByIp(ip: String) = {
    ???
  }
}
object IpUtils {

  def getCity(ip:String) = {
    IpHelper.findRegionByIp(ip)
  }

  def main(args: Array[String]) {
    println(getCity("218.75.35.226"))
  }
}
