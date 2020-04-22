package experimentStats.transformers

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.sql.functions.col
import org.json4s.JString
import org.json4s.JsonAST.JArray
import org.json4s.jackson.JsonMethods
import scalaj.http.Http
import util.{AppSparkSession, IHttpReq, Transformer, HttpReq}

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
            col("environment"),
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

    private val host = sys.env.getOrElse("PRIVATE_EXPERIMENTS_SERVICE_HOST", "")
    private val port = sys.env.getOrElse("PRIVATE_EXPERIMENTS_SERVICE_PORT", "")
    private var batchSize = 100
    private var http: IHttpReq = new HttpReq()

    def getVariationForUser(experimentToUserId: Dataset[Row]): DataFrame = {

        val numberOfBatches = 1 + (experimentToUserId.count().toInt/batchSize)
        val batches = experimentToUserId.randomSplit(Array.fill(numberOfBatches)(0.1))

        var result: RDD[UserVariation] = AppSparkSession.sparkContext.emptyRDD

        batches.foreach(batch => {
            val variationSessions = http.request(
                s"http://$host:$port/experiment/variations",
                batch.toJSON.collect().mkString("[",",","]")
            )

            if(variationSessions != null){
                val parsedVariationSessions = JsonMethods.parse(variationSessions).asInstanceOf[JArray].arr
                val variationSessionsRDD = AppSparkSession.sparkContext.parallelize(parsedVariationSessions)
                result = result.union(variationSessionsRDD.map[UserVariation](
                    value => UserVariation(
                        projectId = (value \ "projectId").asInstanceOf[JString].values,
                        experimentName = (value \ "experimentName").asInstanceOf[JString].values,
                        userId = (value \ "userId").asInstanceOf[JString].values,
                        variation = (value \ "variation").asInstanceOf[JString].values
                    )
                ))
            }
        })

        AppSparkSession.spark.createDataFrame(result)
    }

    def setHttp(http: IHttpReq): Unit = {
        this.http = http
    }

    def setBatchSize(batchSize: Int): Unit = {
        this.batchSize = batchSize
    }
}

case class UserVariation(projectId: String, experimentName: String, userId: String, variation: String)
