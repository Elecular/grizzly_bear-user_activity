package util

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext

/**
  * A Singleton Spark Session Wrapper. Use this to accesss spark session
  */
object AppSparkSession {

    private val master = sys.env("MASTER");
    private val appName = "UserActivityProcessor"

    val spark: SparkSession = SparkSession
    .builder()
    .master(master)
    .config("spark.mongodb.input.database", "")
    .config("spark.mongodb.input.collection", "")
    .config("spark.mongodb.output.uri", sys.env("MONGODB_URL"))
    .config("spark.mongodb.output.database", sys.env("MONGODB_DATABASE"))
    .appName(appName)
    .getOrCreate()

    sparkContext.setLogLevel("WARN")

    def sparkContext: SparkContext = {
        spark.sparkContext
    }
}
