import org.apache.spark.sql.SparkSession

object UserActivityProcessor {

    def main(args: Array[String])  { 
        // prints Hello World 
        AppSparkSession;
        println(MongoConnector.loadCollection("user_session").count());
        println("Hello World!")  
    } 
    
    
    //println(MongoConnector.loadCollection("user_session").count());
    //println("Hello Spark!");
}