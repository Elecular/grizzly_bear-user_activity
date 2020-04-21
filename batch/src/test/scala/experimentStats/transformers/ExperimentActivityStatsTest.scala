package experimentStats.transformers

import org.apache.spark.sql.DataFrame
import org.scalatest.funsuite.AnyFunSuite
import util.AppSparkSession
import util.AppSparkSession.spark.implicits._

class ExperimentActivityStatsTest extends AnyFunSuite {

    test("Can aggregate experiment sessions") {
        val expSessions = Seq(
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_1", 28, Array("all")),
            ("project_id_1", "prod", "user_id_2", "exp_1", "variation1", "session_id_2", 28, Array("all", "one")),
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_3", 28, Array("all")),
            ("project_id_1", "prod", "user_id_3", "exp_1", "variation2", "session_id_4", 28, Array("all")),
            ("project_id_1", "prod", "user_id_4", "exp_1", "variation2", "session_id_5", 28, Array("all"))
        ).toDF("projectId", "environment", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")

        val userActivity = Seq(
            ("session_id_1", "buy", 15),
            ("session_id_2", "buy", 20),
            ("session_id_2", "buy", 25),
            ("session_id_2", "view", 5),
            ("session_id_4", "buy", 10),
            ("session_id_4", "view", 4),
            ("session_id_5", "buy", 10)
        ).toDF("sessionId", "userAction", "amount")

        val result = ExperimentActivityStats.transform(Map(
            "experimentSessions" -> expSessions,
            "userActivity" -> userActivity
        ))

        val expected = Seq(
            ("project_id_1", "prod", "exp_1", "variation1", "all", "buy", 28, 2, 1, 60),
            ("project_id_1", "prod", "exp_1", "variation1", "all", "view", 28, 1, 1, 5),
            ("project_id_1", "prod", "exp_1", "variation1", "one", "buy", 28, 1, 1, 45),
            ("project_id_1", "prod", "exp_1", "variation1", "one", "view", 28, 1, 1, 5),
            ("project_id_1", "prod", "exp_1", "variation2", "all", "view", 28, 1, 1, 4),
            ("project_id_1", "prod", "exp_1", "variation2", "all", "buy", 28, 2, 1, 20)
        ).toDF("projectId", "environment", "experimentName", "variation", "segment", "userAction", "hourNumber", "count", "dayNumber", "amount")

        assert(result
        .select("_id")
        .distinct
        .count() == 6)

        assertDataFramesAreEqual(expected, result, Array("projectId", "experimentName", "variation", "segment", "userAction", "hourNumber", "count", "dayNumber", "amount"))
    }

    test("Can aggregate experiment sessions by environment") {
        val expSessions = Seq(
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_1", 28, Array("all")),
            ("project_id_1", "prod", "user_id_2", "exp_1", "variation1", "session_id_2", 28, Array("all", "one")),
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_3", 28, Array("all")),
            ("project_id_1", "stage", "user_id_3", "exp_1", "variation1", "session_id_4", 28, Array("all")),
            ("project_id_1", "stage", "user_id_4", "exp_1", "variation1", "session_id_5", 28, Array("all"))
        ).toDF("projectId", "environment", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")

        val userActivity = Seq(
            ("session_id_1", "buy", 15),
            ("session_id_2", "buy", 20),
            ("session_id_2", "buy", 25),
            ("session_id_2", "view", 5),
            ("session_id_4", "buy", 10),
            ("session_id_4", "view", 4),
            ("session_id_5", "buy", 10)
        ).toDF("sessionId", "userAction", "amount")

        val result = ExperimentActivityStats.transform(Map(
            "experimentSessions" -> expSessions,
            "userActivity" -> userActivity
        ))

        val expected = Seq(
            ("project_id_1", "prod", "exp_1", "variation1", "all", "buy", 28, 2, 1, 60),
            ("project_id_1", "prod", "exp_1", "variation1", "all", "view", 28, 1, 1, 5),
            ("project_id_1", "prod", "exp_1", "variation1", "one", "buy", 28, 1, 1, 45),
            ("project_id_1", "prod", "exp_1", "variation1", "one", "view", 28, 1, 1, 5),
            ("project_id_1", "stage", "exp_1", "variation1", "all", "view", 28, 1, 1, 4),
            ("project_id_1", "stage", "exp_1", "variation1", "all", "buy", 28, 2, 1, 20)
        ).toDF("projectId", "environment", "experimentName", "variation", "segment", "userAction", "hourNumber", "count", "dayNumber", "amount")

        assertDataFramesAreEqual(expected, result, Array("projectId", "experimentName", "variation", "segment", "userAction", "hourNumber", "count", "dayNumber", "amount"))
    }

    test("Cannot aggregate when there are no user activity") {
        val expSessions = Seq(
            ("project_id_1", "user_id_1", "exp_1", "variation1", "session_id_1", 28, Array("all"))
        ).toDF("projectId", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")
        val userActivity = AppSparkSession.spark.emptyDataFrame

        val result = ExperimentActivityStats.transform(Map(
            "experimentSessions" -> expSessions,
            "userActivity" -> userActivity
        ))

        assert(result.count() == 0)
    }

    test("Cannot aggregate when there are no experiment sessions") {
        val expSessions = AppSparkSession.spark.emptyDataFrame
        val userActivity = Seq(
            ("session_id_1", "buy", "amount")
        ).toDF("sessionId", "userAction", "amount")

        val result = ExperimentActivityStats.transform(Map(
            "experimentSessions" -> expSessions,
            "userActivity" -> userActivity
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
