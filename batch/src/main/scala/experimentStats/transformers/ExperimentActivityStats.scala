package experimentStats.transformers

import org.apache.spark.sql.DataFrame
import util.{AppSparkSession, Transformer}
import org.apache.spark.sql.functions._

/**
  * This transformer aggregates the experiment sessions and
  */
object ExperimentActivityStats extends Transformer {


    override def transform(dataFrames: Map[String, DataFrame]): DataFrame = {
        val experimentSessions = dataFrames("experimentSessions")
        val userActivity = dataFrames("userActivity")

        if(experimentSessions.isEmpty || userActivity.isEmpty)
            return AppSparkSession.spark.emptyDataFrame

        val aggregatedUserActivity = userActivity.select(
            col("sessionId"),
            col("userAction"),
            col("amount")
        )
        .groupBy("sessionId", "userAction")
        .agg(sum("amount").alias("amount"))

        experimentSessions
        .join(aggregatedUserActivity, Seq("sessionId"), "inner")
        .select(
            col("projectId"),
            col("environment"),
            col("experimentName"),
            col("variation"),
            col("hourNumber"),
            explode(col("segments")) as "segment",
            col("userAction"),
            col("amount")
        )
        .groupBy(
            col("projectId"),
            col("environment"),
            col("experimentName"),
            col("variation"),
            col("segment"),
            col("hourNumber"),
            col("userAction")
        )
        .agg(sum("amount").alias("amount"), count("amount").alias("count"))
        .withColumn("_id", sha2(concat(
            col("projectId"),
            col("environment"),
            col("experimentName"),
            col("variation"),
            col("segment"),
            col("hourNumber"),
            col("userAction")
        ), 256))
        .withColumn("dayNumber", floor(col("hourNumber")/24))
    }
}