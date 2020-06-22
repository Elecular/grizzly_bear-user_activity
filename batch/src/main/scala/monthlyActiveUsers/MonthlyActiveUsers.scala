package monthlyActiveUsers

import java.text.SimpleDateFormat

import experimentStats.extractors.UserSessionExtractor
import org.apache.spark.sql.DataFrame
import util.{AggregationInterval, Batch, MongoConnector}
import org.apache.spark.sql.functions._

/**
  * Used for computing monthly active users
  */
object MonthlyActiveUsers extends Batch("MonthlyActiveUsers", AggregationInterval.Daily, 1) {

    /*
     * Calculates monthly active users at given date.
     * It is calculated by checking number of unique visitors in the past 30 days.
     * The startTime is taken as date
     * The endTime is not used. It is ignored
     */
    override def execute(startTime: Long, endTime: Long): Unit = {

        val monthLength = AggregationInterval.Daily * 30
        val dateFormat = new SimpleDateFormat("MM-yyyy")

        //We are getting data from past 30 days
        val userSessions = UserSessionExtractor.extract(startTime - monthLength, startTime)

        //Monthly Active Users (MAU)
        val mau = calculateUniqueUsersPerProject(userSessions)
        .withColumn("date", typedLit(dateFormat.format(startTime)))
        .withColumn("_id", sha2(concat(
            col("date"),
            col("projectId")
        ), 256))
        MongoConnector.writeToCollection("project_mau", mau)
    }

    /*
     * Calculates number of unique visitors per project from given userSessions
     */
    def calculateUniqueUsersPerProject(userSessions: DataFrame): DataFrame = {
        userSessions
        .select(
            col("userId"),
            col("projectId")
        )
        .distinct()
        .groupBy("projectId")
        .count()
    }
}