import org.apache.spark.rdd.RDD
import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config._

object MongoConnector {
    
    def loadCollection(collectionName: String): RDD[String] = {
        val readConfig = ReadConfig(
            Map(
                "uri" -> sys.env("MONGODB_URL"),
                "database" -> sys.env("MONGODB_DATABASE"),
                "collection" -> collectionName 
            ), 
            Some(ReadConfig(AppSparkSession.getSparkContext()))
        );
        return MongoSpark.load(
            AppSparkSession.getSparkContext(), readConfig
        );
    }

}