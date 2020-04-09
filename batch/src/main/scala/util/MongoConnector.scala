package util

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config._
import org.apache.spark.sql.DataFrame;

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

object MongoConnector {

  /**
    * Loads collection from Mongo Database
    *
    * @param collectionName
    * @return
    */
  def loadCollection[D <: Product: TypeTag](
      collectionName: String
  ): DataFrame = {
    val readConfig = ReadConfig(
      Map(
        "uri" -> sys.env("MONGODB_URL"),
        "database" -> sys.env("MONGODB_DATABASE"),
        "collection" -> collectionName
      )
    );
    return MongoSpark.load[D](
      AppSparkSession.getSpark(),
      readConfig
    );
  }

}
