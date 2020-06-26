package dailyPerformanceMetrics

import java.text.SimpleDateFormat

import experimentStats.extractors.{UserActivityExtractor, UserSessionExtractor}
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import util.{AggregationInterval, Batch, MongoConnector}

/**
  * Used for calculating daily performance metrics for all projects
  * The metrics include
  * Number of sessions,
  * Ad Clicks
  * Number Of Micro-transactions
  * Revenue from Micro-transactions
  * Player Retention
  */
object DailyPerformanceMetrics extends Batch("DailyPerformanceMetrics", AggregationInterval.Daily, 1) {

    /*
     * Calculates the performance of all projects for given date
     * The startTime is taken as date
     * The endTime is not used. It is ignored
     */
    override def execute(startTime: Long, endTime: Long): Unit = {

        val dateFormat = new SimpleDateFormat("MM-yyyy")

        val userSessions = UserSessionExtractor.extract(startTime - AggregationInterval.Daily, startTime)
        val userActivity = UserActivityExtractor.extract(startTime - AggregationInterval.Daily, startTime)

        val performanceMetrics = calculatePerformance(userSessions, userActivity)
        .withColumn("date", typedLit(dateFormat.format(startTime)))
        .withColumn("_id", sha2(concat(
            col("date"),
            col("userAction"),
            col("projectId")
        ), 256))
        
        MongoConnector.writeToCollection("project_performance", performanceMetrics)
    }

    /*
     * Calculates the number of each events for each project
     */
    def calculatePerformance(userSessions: DataFrame, userActivity: DataFrame): DataFrame = {

        val sessionMetric = calculateNumberOfSessions(userSessions)

        userActivity
        .join(
            userSessions,
            userSessions("_id") === userActivity("sessionId"),
            "inner"
        )
        .groupBy(
            "projectId",
            "userAction"
        )
        .agg(
            sum("amount").alias("amount")
        )
        .join(
            sessionMetric,
            Seq("projectId"),
            "inner"
        )
    }

    /*
     * Calculates number of sessions per project
     */
    def calculateNumberOfSessions(userSessions: DataFrame): DataFrame = {
        userSessions
        .select(
            col("_id"),
            col("projectId")
        )
        .groupBy("projectId")
        .count()
        .withColumnRenamed("count", "numberOfSessions")
    }
}