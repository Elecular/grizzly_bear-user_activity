package experimentStats.extractors

import org.apache.spark.sql.DataFrame
import org.json4s.JString
import org.json4s.JsonAST.{JArray, JInt, JNull}
import org.json4s.jackson.JsonMethods
import scalaj.http.Http
import util.{AppSparkSession, Extractor}

object RunningExperimentsExtractor extends Extractor {

    private val host = sys.env("PRIVATE_EXPERIMENTS_SERVICE_HOST")
    private val port = sys.env("PRIVATE_EXPERIMENTS_SERVICE_PORT")

    def extract(startTime: Long, endTime: Long): DataFrame = {
        val experiments = Http(s"http://$host:$port/bulk/experiments/?startTime=$startTime&endTime=$endTime").asString
        val parsedExperiments = JsonMethods.parse(experiments.body).asInstanceOf[JArray].arr
        val experimentsRDD = AppSparkSession.sparkContext.parallelize(parsedExperiments)

        AppSparkSession.spark
        .createDataFrame(
            experimentsRDD.map[Experiment](
                value => Experiment(
                    projectId = (value \ "projectId").asInstanceOf[JString].values,
                    experimentName = (value \ "_id" \ "experimentName").asInstanceOf[JString].values,
                    startTime = (value \ "startTime").asInstanceOf[JInt].values.toLong,
                    endTime = (value \ "endTime") match {
                        case int: JInt => Some(int.values.toLong)
                        case _ => None
                    }
                )
            )
        )
    }
}

case class Experiment(projectId: String, experimentName: String, startTime: Long, endTime: Option[Long])
