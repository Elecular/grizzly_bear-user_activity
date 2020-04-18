package experimentStats.transformers

import org.apache.spark.sql.DataFrame
import util.Transformer
import org.apache.spark.sql.functions._

/**
  * This transformer aggregates the experiment sessions and
  */
object ExperimentActivityStats extends Transformer {


    override def transform(dataFrames: Map[String, DataFrame]): DataFrame = {
        val experimentSessions = dataFrames("experimentSessions")
        val userActivity = dataFrames("userActivity").select(
            col("sessionId"),
            col("userAction")
        ).distinct

        experimentSessions
        .join(userActivity, Seq("sessionId"), "inner")
        .select(
            col("projectId"),
            col("experimentName"),
            col("variation"),
            col("hourNumber"),
            explode(col("segments")) as "segment",
            col("userAction")
        )
        .groupBy(
            col("projectId"),
            col("experimentName"),
            col("variation"),
            col("segment"),
            col("hourNumber"),
            col("userAction")
        )
        .count()
        .withColumn("dayNumber", floor(col("hourNumber")/24))
    }
}