package util

import org.apache.spark.sql.DataFrame

trait Loader {

    /**
      * Implement loading of data to a destination data store
      */
    def load(dataFrame: DataFrame): Unit
}
