package util

import org.apache.spark.sql.types.{BooleanType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{AnalysisException, DataFrame, Row}
import org.apache.spark.sql.functions._

/**
  * Extend this class for implementing a batch.
  * A Batch is a pipeline of jobs that perform the actual computation
  *
  * @param name                Name of the batch
  * @param aggregationInterval This is the amount of data that is being aggregated.
  *                            If you are aggregating hourly data, this is set to Interval.Hourly
  *                            If you are aggregating daily data, this is set to Interval.Daily
  * @param timeOffset          You can add a time offset to process older data. (Has to be equal to or bigger than 1)
  *                            If you choose to aggregate Hourly data with timeOffset set to 6, you will process data that is at least 6 hours old
  *                            If you choose to aggregate Daily data with timeOffset set to 10, you will process data that is at least 10 days old
  *
  *                            This batch is idempotent in case of failure. If your batch fails for a certain hour, you can simply rerun the batch. It will automatically pick up data for the failed hour.
  */
abstract class Batch(name: String, aggregationInterval: Long, timeOffset: Long = 1) {

    if (timeOffset < 1) throw new Error("Time Offset has to be bigger than or equal to 1")

    BatchProcessor.subscribeBatch(name, this);

    /**
      * Runs the batch
      * It gets timestamp of last successful batch and runs the next batch based on that
      */
    def run(): Unit = {
        val currentUnitTime = System.currentTimeMillis() / aggregationInterval // Gets the current hour number / day number / week number etc.
        val lastBatchRunTimestamp = getLastSuccessfulRunTimestamp(name)
        .getOrElse((currentUnitTime - timeOffset) * aggregationInterval) //If no batches ran previously, we are defaulting to the time offset that was requested
        val startTime = lastBatchRunTimestamp
        val endTime = startTime + aggregationInterval
        var success = true
        var error = ""

        if(startTime > System.currentTimeMillis() - (timeOffset * aggregationInterval))  {
            println(s"Its too early to calculate data for $startTime to $endTime")
            return
        }
        else {
            println(s"Executing Batch for $startTime to $endTime");
        }

        try {
            execute(startTime, endTime - 1)
        } catch {
            case e: Exception => {
                success = false
                error = e.getMessage
            }
        }
        logBatchRun(startTime, endTime, success, error)
    }

    /**
      * Gets timestamp of last successful batch run
      */
    private def getLastSuccessfulRunTimestamp(batchName: String): Option[Long] = {
        try {
            Option(MongoConnector
            .loadCollection("batch_run")
            .filter(s"batchName='$batchName'")
            .filter(col("success"))
            .agg(max("endTime"))
            .head().getAs[Long]("max(endTime)"))
        } catch {
            case ae: AnalysisException => {
                println(ae)
                Option.empty
            }
        }
    }

    /**
      * Logs a batch run
      */
    private def logBatchRun(startTime: Long, endTime: Long, success: Boolean, error: String): Unit = {
        val log = AppSparkSession.sparkContext.parallelize(Seq(
            Row(name, startTime, endTime, success, error, System.currentTimeMillis())
        ))

        val schema = new StructType()
        .add(StructField("batchName", StringType, nullable = true))
        .add(StructField("startTime", LongType, nullable = true))
        .add(StructField("endTime", LongType, nullable = true))
        .add(StructField("success", BooleanType, nullable = true))
        .add(StructField("error", StringType, nullable = true))
        .add(StructField("timestamp", LongType, nullable = true))
        MongoConnector.writeToCollection("batch_run", AppSparkSession.spark.createDataFrame(log, schema))
    }

    /**
      * Executes the batch
      */
    def execute(startTime: Long, endTime: Long): Unit
}

object AggregationInterval extends Enumeration {
    val Hourly: Long = 60 * 60 * 1000
    val Daily: Long = Hourly * 24
}