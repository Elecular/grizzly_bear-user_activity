package monthlyActiveUsers

import org.apache.spark.sql.DataFrame
import org.scalatest.funsuite.AnyFunSuite
import util.AppSparkSession.spark.implicits._

class MonthlyActiveUsersTest extends AnyFunSuite {

    test("Can calculate number of unique visitors per project") {
        val userSessions = Seq(
            ("project_id_1", "user_id_1"),
            ("project_id_1", "user_id_2"),
            ("project_id_2", "user_id_1"),
            ("project_id_2", "user_id_3"),
            ("project_id_2", "user_id_4")
        ).toDF("projectId", "userId")

        val result = MonthlyActiveUsers.calculateUniqueUsersPerProject(userSessions)
        val expected = Seq(
            ("project_id_1", "2"),
            ("project_id_2", "3")
        ).toDF("projectId", "count")

        assertDataFramesAreEqual(result, expected, Array("projectId", "count"))
    }

    def assertDataFramesAreEqual(a: DataFrame, b: DataFrame, cols: Array[String]): Unit = {
        val aPrime = a.select(cols.head, cols.tail: _*).groupBy(cols.head, cols.tail: _*).count()
        val bPrime = b.select(cols.head, cols.tail: _*).groupBy(cols.head, cols.tail: _*).count()

        assert(bPrime.except(aPrime).count() == 0)
        assert(aPrime.except(bPrime).count() == 0)
    }
}