package experimentStats.transformers

import org.apache.spark.sql.DataFrame
import util.Transformer
import org.apache.spark.sql.functions._

/**
  * This transformer aggregates the experiment sessions and
  */
object ExperimentSessionStats extends Transformer {


    override def transform(dataFrames: Map[String, DataFrame]): DataFrame = {
        val experimentSessions = dataFrames("experimentSessions")

        experimentSessions
        .select(
            col("projectId"),
            col("experimentName"),
            col("variation"),
            col("hourNumber"),
            explode(col("segments")) as "segment"
        )
        .groupBy(
            col("projectId"),
            col("experimentName"),
            col("variation"),
            col("segment"),
            col("hourNumber")
        )
        .count()
        .withColumn("dayNumber", floor(col("hourNumber")/24))
    }
}
