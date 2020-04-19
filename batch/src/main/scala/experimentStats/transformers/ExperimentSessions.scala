package experimentStats.transformers

import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.sql.functions.col
import org.json4s.JString
import org.json4s.JsonAST.JArray
import org.json4s.jackson.JsonMethods
import scalaj.http.Http
import util.{AppSparkSession, Transformer}

/**
  * This transformer takes a Data frame of User Session and another Data frame of Experiments.
  * It creates a mapping between an experiment and all user sessions that happened in that experiment.
  */
object ExperimentSessions extends Transformer {

    private var userVariationMapper: IUserVariationMapper = VariationMapper

    /**
      * Takes:
      * userSessions
      * experiments
      */
    override def transform(dataFrames: Map[String, DataFrame]): DataFrame = {
        val userSession = dataFrames("userSessions").withColumnRenamed("_id", "sessionId")
        val experiments = dataFrames("experiments")

        if(userSession.isEmpty || experiments.isEmpty)
            return AppSparkSession.spark.emptyDataFrame

        //Mapping between all user sessions and the experiments they belonged to
        val experimentSessions = userSession
        .join(experiments, Seq("projectId"), "inner")
        .filter(
            col("timestamp") >= col("startTime") &&
            col("timestamp") <= col("endTime")
        )
        .select(
            col("sessionId"),
            col("projectId"),
            col("experimentName"),
            col("userId"),
            col("hourNumber"),
            col("segments")
        )

        //Calculating the variation each user was assigned to
        val userVariation = userVariationMapper.getVariationForUser(
            experimentSessions
            .select(
                col("projectId"),
                col("experimentName"),
                col("userId")
            ).distinct
        )
        experimentSessions
        .join(userVariation, Seq("projectId", "experimentName", "userId"), "inner")
    }

    /**
      * Sets the user variation mapper. This is used for mocking during testing
      */
    def setUserVariationMapper(variationMapper: IUserVariationMapper): Unit = {
        userVariationMapper = variationMapper
    }
}

object VariationMapper extends IUserVariationMapper {

    private val host = sys.env("PRIVATE_EXPERIMENTS_SERVICE_HOST")
    private val port = sys.env("PRIVATE_EXPERIMENTS_SERVICE_PORT")

    override def getVariationForUser(experimentToUserId: Dataset[Row]): DataFrame = {
        val variationSessions = Http(s"http://$host:$port/experiment/variations")
        .postData(experimentToUserId.toJSON.collect().mkString("[",",","]"))
        .header("Content-type", "application/json")
        .asString

        val parsedVariationSessions = JsonMethods.parse(variationSessions.body).asInstanceOf[JArray].arr
        val variationSessionsRDD = AppSparkSession.sparkContext.parallelize(parsedVariationSessions)

        AppSparkSession.spark
        .createDataFrame(
            variationSessionsRDD.map[UserVariation](
                value => UserVariation(
                    projectId = (value \ "projectId").asInstanceOf[JString].values,
                    experimentName = (value \ "experimentName").asInstanceOf[JString].values,
                    userId = (value \ "userId").asInstanceOf[JString].values,
                    variation = (value \ "variation").asInstanceOf[JString].values
                )
            )
        )
    }
}

case class UserVariation(projectId: String, experimentName: String, userId: String, variation: String)
