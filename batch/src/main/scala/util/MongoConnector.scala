package util

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config._
import org.apache.spark.sql.{DataFrame, SaveMode}

import scala.reflect.runtime.universe._

object MongoConnector {

    /**
      * Loads collection from Mongo Database
      */
    def loadCollection[D <: Product : TypeTag](collectionName: String): DataFrame = {
        val readConfig = ReadConfig(
            Map(
                "uri" -> sys.env("MONGODB_URL"),
                "database" -> sys.env("MONGODB_DATABASE"),
                "collection" -> collectionName,
                "readPreference.name" -> "secondaryPreferred"
            )
        )
         MongoSpark.load[D](
            AppSparkSession.spark,
            readConfig
        )
    }

    def writeToCollection(collectionName: String, dataFrame: DataFrame): Unit = {
        MongoSpark.save(dataFrame.write.option("collection", collectionName).mode(SaveMode.Append))
    }

}
