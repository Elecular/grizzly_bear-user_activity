import org.apache.spark.sql.SparkSession

object UserActivityProcessor {

    def main(args: Array[String])  { 
        AppSparkSession;
        println(MongoConnector.loadCollection("user_session").count());
        println("Hello World!")  
    } 
}