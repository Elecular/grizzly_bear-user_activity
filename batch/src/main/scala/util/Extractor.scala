package util

import org.apache.spark.sql.DataFrame

trait Extractor {

    /**
      * Implement extraction of Data frames from data stores
      */
    def extract(startTime: Long, endTime: Long): DataFrame
}