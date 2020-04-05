import org.apache.spark.sql.SparkSession;
import org.apache.spark.SparkContext

/**
  * A Singleton Spark Session Wrapper. Use this to accesss spark session
  */
object AppSparkSession {

    private val master = sys.env("MASTER");
    private val appName = "UserActivityProcessor";
    
    private val spark = SparkSession.builder()
        .master(master)
        .appName(appName)
        .getOrCreate();

    /**
      * Gets the spark session
      */
    def getSpark(): SparkSession = {
        return spark;
    }

    def getSparkContext(): SparkContext = {
        return spark.sparkContext;
    }
}