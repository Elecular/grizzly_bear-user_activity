package experimentStats.transformers
import experimentStats.extractors.Experiment
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.json4s.JsonAST.{JObject, JString}
import org.json4s.jackson.JsonMethods
import org.scalatest.funsuite.AnyFunSuite
import util.{AppSparkSession, IHttpReq}
import util.AppSparkSession.spark.implicits._

class ExperimentSessionsTest extends AnyFunSuite {

    test("Can map users to experiments") {
        val userSessions = Seq(
            ("session_id_1", "project_id_1", "prod", "user_id_1", Array("all", "one", "three"), 1700L, 10),
            ("session_id_11", "project_id_1", "prod", "user_id_1", Array("all", "one", "three"), 100000L, 10),
            ("session_id_2", "project_id_1", "prod", "user_id_2", Array("all", "one", "three"), 2300L, 10),
            ("session_id_3", "project_id_1", "prod", "user_id_3", Array("all", "one", "three"), 10000L, 10),
            ("session_id_4", "project_id_2", "prod", "user_id_1", Array("all", "one", "three"), 4000L, 10),
            ("session_id_5", "project_id_2", "prod", "user_id_2", Array("all", "one", "three"), 15000L, 10),
            ("session_id_6", "project_id_2", "prod", "user_id_3", Array("all", "one", "three"), 17000L, 10),
            ("session_id_7", "project_id_2", "prod", "user_id_3", Array("all", "one", "three"), 18000L, 10),
            ("session_id_8", "project_id_2", "prod", "user_id_4", Array("all", "one", "three"), 25000L, 10),
            ("session_id_stage_1", "project_id_1", "stage", "user_id_1", Array("all", "one", "three"), 1700L, 10),
            ("session_id_stage_2", "project_id_1", "stage", "user_id_2", Array("all", "one", "three"), 2300L, 10),
            ("session_id_stage_3", "project_id_1", "stage", "user_id_3", Array("all", "one", "three"), 10000L, 10)
        ).toDF("_id", "projectId", "environment", "userId",  "segments", "timestamp", "hourNumber")

        val experiments = Seq(
            Experiment("project_id_1", "exp_4", 50000L, None),
            Experiment("project_id_1", "exp_1", 1000L, Some(2000L)),
            Experiment("project_id_1", "exp_2", 1500L, Some(2500L)),
            Experiment("project_id_1", "exp_3", 4000L, Some(5000L)),
            Experiment("project_id_2", "exp_1", 500L, Some(6000L)),
            Experiment("project_id_2", "exp_2", 10000L, Some(20000L))
        ).toDF("projectId", "experimentName", "startTime", "endTime")
        //Mocking response from experiments service
        val mockRes1 = Seq(
            ("project_id_2", "exp_2", "user_id_2", "variation2"),
            ("project_id_2", "exp_2", "user_id_3", "variation1")
        ).toDF("projectId", "experimentName", "userId", "variation").toJSON.collect.mkString("[",",","]")

        val mockRes2 = Seq(
            ("project_id_1", "exp_1", "user_id_1", "variation1"),
            ("project_id_1", "exp_4", "user_id_1", "variation2"),
            ("project_id_1", "exp_2", "user_id_1", "variation2"),
            ("project_id_1", "exp_2", "user_id_2", "variation1"),
            ("project_id_2", "exp_1", "user_id_1", "variation2")
        ).toDF("projectId", "experimentName", "userId", "variation").toJSON.collect.mkString("[",",","]")

        MockHttpReq.setMockResponse(Array(mockRes1, mockRes2) ++ Array.fill(30)(null))
        VariationMapper.setBatchSize(2)
        VariationMapper.setHttp(MockHttpReq)

        val result = ExperimentSessions.transform(Map(
            "userSessions" -> userSessions,
            "experiments"-> experiments
        ))
        val expected = Seq(
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_1", 10, Array("all", "one", "three")),
            ("project_id_1", "prod", "user_id_1", "exp_4", "variation2", "session_id_11", 10, Array("all", "one", "three")),
            ("project_id_1", "prod", "user_id_1", "exp_2", "variation2", "session_id_1", 10, Array("all", "one", "three")),
            ("project_id_1", "prod", "user_id_2", "exp_2", "variation1", "session_id_2", 10, Array("all", "one", "three")),
            ("project_id_2", "prod", "user_id_1", "exp_1", "variation2", "session_id_4", 10, Array("all", "one", "three")),
            ("project_id_2", "prod", "user_id_2", "exp_2", "variation2", "session_id_5", 10, Array("all", "one", "three")),
            ("project_id_2", "prod", "user_id_3", "exp_2", "variation1", "session_id_6", 10, Array("all", "one", "three")),
            ("project_id_2", "prod", "user_id_3", "exp_2", "variation1", "session_id_7", 10, Array("all", "one", "three")),
            ("project_id_1", "stage", "user_id_1", "exp_1", "variation1", "session_id_stage_1", 10, Array("all", "one", "three")),
            ("project_id_1", "stage", "user_id_1", "exp_2", "variation2", "session_id_stage_1", 10, Array("all", "one", "three")),
            ("project_id_1", "stage", "user_id_2", "exp_2", "variation1", "session_id_stage_2", 10, Array("all", "one", "three"))
        ).toDF("projectId", "environment", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")

        assert(result.count() == expected.count())
        assert(
            result
            .join(
                expected,
                Seq("projectId", "userId", "experimentName", "variation", "sessionId")
            ).count() == expected.count()
        )
        assertDataFramesAreEqual(expected, result, Array("projectId", "userId", "experimentName", "variation", "sessionId", "environment"))
    }

    test("Cannot map users when there are no user sessions") {
        val userSessions = AppSparkSession.spark.emptyDataFrame

        val experiments = Seq(
            ("project_id_1", "exp_1", 1000L, 2000L)
        ).toDF("projectId", "experimentName", "startTime", "endTime")

        MockUserVariationMapper.setVariationMapping(
            Seq(
                ("project_id_1", "exp_1", "user_id_1", "variation1")
            ).toDF("projectId", "experimentName", "userId", "variation")
        )
        ExperimentSessions.setUserVariationMapper(MockUserVariationMapper)

        val result = ExperimentSessions.transform(Map(
            "userSessions" -> userSessions,
            "experiments"-> experiments
        ))

        assert(result.count() == 0)
    }

    test("Cannot map users when there are no experiments") {
        val userSessions = Seq(
            ("session_id_1", "project_id_1", "user_id_1", ("all", "one", "three"), 1700L, 10)
        ).toDF("_id", "projectId", "userId",  "segments", "timestamp", "hourNumber")

        val experiments = AppSparkSession.spark.emptyDataFrame

        MockUserVariationMapper.setVariationMapping(
            Seq(
                ("project_id_1", "exp_1", "user_id_1", "variation1")
            ).toDF("projectId", "experimentName", "userId", "variation")
        )
        ExperimentSessions.setUserVariationMapper(MockUserVariationMapper)

        val result = ExperimentSessions.transform(Map(
            "userSessions" -> userSessions,
            "experiments"-> experiments
        ))

        assert(result.count() == 0)
    }

    def assertDataFramesAreEqual(a: DataFrame, b: DataFrame, cols: Array[String]): Unit = {
        val aPrime = a.select(cols.head, cols.tail: _*).groupBy(cols.head, cols.tail: _*).count()
        val bPrime = b.select(cols.head, cols.tail: _*).groupBy(cols.head, cols.tail: _*).count()

        assert(bPrime.except(aPrime).count() == 0)
        assert(aPrime.except(bPrime).count() == 0)
    }

}

object MockUserVariationMapper extends IUserVariationMapper {

    var variationMapping: DataFrame = _

    /**
      * This method takes a dataset of
      * projectId
      * experimentId
      * userId
      * and returns back the following Data frame
      * projectId
      * experimentId
      * userId
      * variationName
      * It returns the variation the user was assigned to.
      */
    def getVariationForUser(experimentToUserId: Dataset[Row]): DataFrame = {
        variationMapping
    }

    def setVariationMapping(variationMapping: DataFrame): Unit = {
        this.variationMapping = variationMapping
    }
}

object MockHttpReq extends IHttpReq {

    var mockResponses: Array[String] = Array.fill(1)("")
    var index = 0

    override def request(url: String, postData: String): String = {
        val res = mockResponses(index % mockResponses.length)
        index += 1
        res
    }

    def setMockResponse(responses: Array[String]): Unit = {
        mockResponses = responses
        index = 0
    }
}
