import java.text.SimpleDateFormat
import java.util.Date

import org.apache.spark.rdd.RDD
import org.apache.spark.graphx.{EdgeRDD, VertexRDD, Edge, Graph}
import java.util.Properties
import org.apache.spark.sql.{SQLContext, Row}
import org.apache.spark.sql.types._
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Created by lg on 2017/1/16.
 *
 * /opt/spark/sbin# spark-submit --master spark://cloud-03:7077 --executor-memory 8G --total-executor-cores 18 --executor-cores 3 --jars /opt/hive/lib/mysql-connector-java-5.1.35-bin.jar --driver-class-path /opt/hive/lib/mysql-connector-java-5.1.35-bin.jar --class rank /opt/lg/rank.jar
 */

object rank {

  def main(args: Array[String]) {
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    val format = new SimpleDateFormat("hh:mm:ss.SSS")
    val before_rank = new Date();
    println(before_rank)

    /**
     *
     * @param sqlContext 从数据库中读入表Search_Engine_Test_ZXL_2的url,与urllist两列
     * @return 源url,终url
     */
    def getFromMySqlTable(sqlContext: SQLContext): RDD[(String, String)] = {
      val url = "jdbc:mysql://202.117.16.188/search_engine?useUnicode=true&characterEncoding=UTF-8"
      val jdbcDF = sqlContext.read.format("jdbc").options(Map("url" -> url, "driver" -> "com.mysql.jdbc.Driver", "dbtable" -> "Search_Engine_Test_ZXL_2", "user" -> "root", "password" -> "root")).load()
      jdbcDF.registerTempTable("tempTable")
      val row = sqlContext.sql("select URL, URLList from tempTable where URLList is not null and URLList!=\"\"").rdd.map(x => (x(0).toString, x(1).toString)).map(x => {
        var list: List[(String, String)] = List()
        if(x._2!="") {
          for (dst <- x._2.split(";")) {
            list = (x._1, dst) :: list
          }
        }
        list
      }).flatMap(x => x)
      row
    }

    /**
     *
     * @param pairurl 读入 源url,终url
     * @return 初始图
     */
    def contructGraph(pairurl: RDD[(String, String)]): Graph[String, Double] = {
      val vertextemp = (pairurl.map(_._1) ++ pairurl.map(_._2)).distinct.zipWithIndex
      val vertex = vertextemp.map(x => (x._2, x._1))
      val edgetemp = pairurl.join(vertextemp).map(x => (x._2._1, (x._1, x._2._2))).join(vertextemp).map(x => (x._2._1._2, x._2._1._1, x._2._2, x._1))
      val edge = edgetemp.map(x => Edge(x._1, x._3, 0.0))
      Graph(vertex, edge)
    }

    /**
     *
     * @param rank 运行pageRank函数后得到的rank图中的节点（id,score）
     * @param graphV 初始图的节点（id,url）,目的：存入的节点带上url、score
     */
    def saveAllVertex(rank: VertexRDD[Double], graphV: VertexRDD[String]) = {
      val vertex = rank.join(graphV).map(x => (x._2._1, (x._1, x._2._2))).repartition(1).sortByKey(false).map(x => (Array(x._2._1, x._2._2, x._1)))

      val schema = StructType(
        List(
          StructField("id", LongType, true),
          StructField("url", StringType, true),
          StructField("score", DoubleType, true)
        )
      )

      val rowRDD = vertex.map(p => Row(p(0),p(1),p(2)))
      val vertexDataFrame = sqlContext.createDataFrame(rowRDD, schema)
      val prop = new Properties()
      prop.put("user", "root")
      prop.put("password", "root")
      prop.put("driver", "com.mysql.jdbc.Driver")
      vertexDataFrame.write.mode("append").jdbc("jdbc:mysql://202.117.16.188:3306/search_engine?useUnicode=true&characterEncoding=UTF-8", "search_engine.PAGERANK_LG", prop)

    }
    /**
     *
     * @param rank 运行pageRank函数后得到的rank图中的节点（id,score）
     * @param graphE 初始图的边的源终节点（源id,终id）,目的：存入前500的节点之间的边
     */
    def saveTop500Edge(rank: VertexRDD[Double], graphE: EdgeRDD[Double]) = {
      val top500vertexTemp = rank.map(x => (x._2, x._1)).repartition(1).sortByKey(false).top(500)
      val top500vertex = sc.parallelize(top500vertexTemp).map(x => (x._2, x._1))
      val top500edge = graphE.map(e => (e.srcId, e.dstId)).join(top500vertex).map(x => (x._2._1, x._1)).join(top500vertex).map(x => (Array(x._2._1, x._1)))

      val schema = StructType(
        List(
          StructField("srcid", LongType, true),
          StructField("dstid", LongType, true)
        )
      )

      val rowRDD = top500edge.map(p => Row(p(0),p(1)))
      val Top500EdgeDataFrame = sqlContext.createDataFrame(rowRDD, schema)

      val prop = new Properties()
      prop.put("user", "root")
      prop.put("password", "root")
      prop.put("driver", "com.mysql.jdbc.Driver")
      //将数据追加到数据库
      Top500EdgeDataFrame.write.mode("append").jdbc("jdbc:mysql://202.117.16.188:3306/search_engine?useUnicode=true&characterEncoding=UTF-8", "search_engine.TOP500_EDGE_LG", prop)
      }

    val pairurls = getFromMySqlTable(sqlContext)
    val graph = contructGraph(pairurls)
    val ranktemp = graph.pageRank(0.01).vertices
    saveAllVertex(ranktemp, graph.vertices)
    saveTop500Edge(ranktemp, graph.edges)

    val after_rank = new Date();
    println(after_rank)

    sc.stop()
  }

}