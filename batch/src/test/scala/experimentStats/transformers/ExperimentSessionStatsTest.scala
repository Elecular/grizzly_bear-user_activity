package experimentStats.transformers
import org.apache.spark.sql.{Column, DataFrame, Dataset, Row}
import org.scalatest.funsuite.AnyFunSuite
import util.AppSparkSession
import util.AppSparkSession.spark.implicits._

class ExperimentSessionStatsTest extends AnyFunSuite {

    test("Can aggregate experiment sessions by hour and segment") {
        val expSessions = Seq(
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_1", 28, Array("all", "one", "three")),
            ("project_id_1", "prod", "user_id_2", "exp_1", "variation1", "session_id_2", 28, Array("all", "one", "two")),
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_3", 28, Array("all")),
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_4", 80, Array("all", "one", "three")),
            ("project_id_1", "prod", "user_id_2", "exp_1", "variation1", "session_id_5", 80, Array("all", "one", "two"))
        ).toDF("projectId", "environment", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")

        val result = ExperimentSessionStats.transform(Map(
            "experimentSessions" -> expSessions
        ))

        val expected = Seq(
            ("project_id_1", "prod", "exp_1", "variation1", "all", 28, 3, 1),
            ("project_id_1", "prod", "exp_1", "variation1", "one", 28, 2, 1),
            ("project_id_1", "prod", "exp_1", "variation1", "two", 28, 1, 1),
            ("project_id_1", "prod", "exp_1", "variation1", "three", 28, 1, 1),
            ("project_id_1", "prod", "exp_1", "variation1", "all", 80, 2, 3),
            ("project_id_1", "prod", "exp_1", "variation1", "one", 80, 2, 3),
            ("project_id_1", "prod", "exp_1", "variation1", "two", 80, 1, 3),
            ("project_id_1", "prod", "exp_1", "variation1", "three", 80, 1, 3)
        ).toDF("projectId", "environment", "experimentName", "variation", "segment", "hourNumber", "count", "dayNumber")

        assert(result
        .select("_id")
        .distinct
        .count() == 8)

        assertDataFramesAreEqual(expected, result, Array("projectId", "experimentName", "variation", "segment", "hourNumber", "count", "dayNumber"))
    }

    test("Can aggregate experiment sessions by variation") {
        val expSessions = Seq(
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_1", 28, Array("all")),
            ("project_id_1", "prod", "user_id_2", "exp_1", "variation2", "session_id_2", 28, Array("all", "one")),
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation2", "session_id_3", 28, Array("all"))
        ).toDF("projectId", "environment", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")

        val result = ExperimentSessionStats.transform(Map(
            "experimentSessions" -> expSessions
        ))

        val expected = Seq(
            ("project_id_1", "prod", "exp_1", "variation1", "all", 28, 1, 1),
            ("project_id_1", "prod", "exp_1", "variation2", "all", 28, 2, 1),
            ("project_id_1", "prod", "exp_1", "variation2", "one", 28, 1, 1)
        ).toDF("projectId", "environment", "experimentName", "variation", "segment", "hourNumber", "count", "dayNumber")

        assertDataFramesAreEqual(expected, result, Array("projectId", "experimentName", "variation", "segment", "hourNumber", "count", "dayNumber"))
    }

    test("Can aggregate experiment sessions by environment") {
        val expSessions = Seq(
            ("project_id_1", "prod", "user_id_1", "exp_1", "variation1", "session_id_1", 28, Array("all")),
            ("project_id_1", "stage", "user_id_2", "exp_1", "variation1", "session_id_2", 28, Array("all", "one")),
            ("project_id_1", "stage", "user_id_1", "exp_1", "variation1", "session_id_3", 28, Array("all"))
        ).toDF("projectId", "environment", "userId", "experimentName", "variation", "sessionId", "hourNumber", "segments")

        val result = ExperimentSessionStats.transform(Map(
            "experimentSessions" -> expSessions
        ))

        val expected = Seq(
            ("project_id_1", "prod", "exp_1", "variation1", "all", 28, 1, 1),
            ("project_id_1", "stage", "exp_1", "variation1", "all", 28, 2, 1),
            ("project_id_1", "stage", "exp_1", "variation1", "one", 28, 1, 1)
        ).toDF("projectId", "environment", "experimentName", "variation", "segment", "hourNumber", "count", "dayNumber")

        assertDataFramesAreEqual(expected, result, Array("projectId", "experimentName", "variation", "segment", "hourNumber", "count", "dayNumber"))
    }

    test("Cannot aggregate when there are not experiment sessions") {
        val expSessions = AppSparkSession.spark.emptyDataFrame

        val result = ExperimentSessionStats.transform(Map(
            "experimentSessions" -> expSessions
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
