package experimentStats.transformers

import org.apache.spark.sql.DataFrame
import util.{AppSparkSession, Transformer}
import org.apache.spark.sql.functions._

/**
  * This transformer aggregates the experiment sessions and
  */
object ExperimentSessionStats extends Transformer {

    def transform(dataFrames: Map[String, DataFrame]): DataFrame = {
        val experimentSessions: DataFrame = dataFrames("experimentSessions")

        if(experimentSessions.isEmpty)
            return AppSparkSession.spark.emptyDataFrame

        experimentSessions
        .select(
            col("projectId"),
            col("environment"),
            col("experimentName"),
            col("variation"),
            col("hourNumber"),
            explode(col("segments")) as "segment"
        )
        .groupBy(
            col("projectId"),
            col("environment"),
            col("experimentName"),
            col("variation"),
            col("segment"),
            col("hourNumber")
        )
        .count()
        .withColumn("_id", sha2(concat(
            col("projectId"),
            col("environment"),
            col("experimentName"),
            col("variation"),
            col("segment"),
            col("hourNumber")
        ), 256))
        .withColumn("dayNumber", floor(col("hourNumber")/24))
    }
}
