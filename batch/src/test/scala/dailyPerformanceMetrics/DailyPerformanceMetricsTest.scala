package dailyPerformanceMetrics

import org.apache.spark.sql.DataFrame
import org.scalatest.funsuite.AnyFunSuite
import util.AppSparkSession.spark.implicits._

class DailyPerformanceMetricsTest extends AnyFunSuite {

    test("Can calculate number of unique visitors per project") {
        val userSessions = Seq(
            ("1", "project_id_1", "user_id_1"),
            ("2", "project_id_1", "user_id_2"),
            ("3", "project_id_2", "user_id_1"),
            ("4", "project_id_2", "user_id_3"),
            ("5", "project_id_2", "user_id_4"),
            ("6", "project_id_3", "user_id_4"),
            ("7", "project_id_3", "user_id_4"),
            ("8", "project_id_3", "user_id_4")
        ).toDF("_id", "projectId", "userId")

        val userActivity = Seq(
            ("1", "click", 5),
            ("1", "click", 3),
            ("2", "click", 4),
            ("2", "buy", 6),
            ("6", "view", 5),
            ("6", "click", 3),
            ("6", "click", 4),
            ("7", "buy", 6)
        ).toDF("sessionId", "userAction", "amount")

        val result = DailyPerformanceMetrics.calculatePerformance(userSessions, userActivity)
        val expected = Seq(
            ("project_id_1", "click", 12, 2),
            ("project_id_1", "buy", 6, 2),
            ("project_id_3", "buy", 6, 3),
            ("project_id_3", "view", 5, 3),
            ("project_id_3", "click", 7, 3)
        ).toDF("projectId", "userAction", "amount", "numberOfSessions")

        assertDataFramesAreEqual(result, expected, Array("projectId", "userAction", "amount", "numberOfSessions"))
    }

    def assertDataFramesAreEqual(a: DataFrame, b: DataFrame, cols: Array[String]): Unit = {
        val aPrime = a.select(cols.head, cols.tail: _*).groupBy(cols.head, cols.tail: _*).count()
        val bPrime = b.select(cols.head, cols.tail: _*).groupBy(cols.head, cols.tail: _*).count()

        assert(bPrime.except(aPrime).count() == 0)
        assert(aPrime.except(bPrime).count() == 0)
    }
}