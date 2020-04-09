import util._
import experimentStats._
import org.apache.spark.sql.DataFrame;

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrameReader

object UserActivityProcessor {

  def main(args: Array[String]) {
    AppSparkSession;
    initBatches();

    if(args.length == 0) 
      throw new Exception("No Arguments Passed")
    BatchProcessor.processBatch(args(0));
  }

  def initBatches() = {
    HourlyExperimentStats;
  }

}
