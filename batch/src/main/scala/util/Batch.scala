package util

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row
import org.bson.Document

abstract class Batch(name: String) {

  BatchProcessor.subscribeBatch(name, this);

  /**
    * Extracts all data from sources.
    *
    * @return
    */
  def getData(): Map[String, DataFrame]

  /**
    * Transforms given data frames into a resulting data frame
    *
    * @param dataFrames
    * @return
    */
  def transform(dataFrames: Map[String, DataFrame]): DataFrame

  /**
    * Stores the resulting dataframe into a datastore
    *
    * @param dataFrame
    */
  def store(dataFrame: DataFrame)
}
