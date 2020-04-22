package util

import org.apache.spark.sql.DataFrame

trait Transformer {

    /**
      * Implement logic for transforming
      *
      */
    def transform(dataFrames: Map[String, DataFrame]): DataFrame

}
