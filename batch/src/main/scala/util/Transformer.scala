package util

import org.apache.spark.sql.DataFrame

abstract class Transformer {

    /**
      * Implement logic for transforming
      *
      */
    def transform(dataFrames: Map[String, DataFrame]): DataFrame

}
