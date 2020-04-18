package experimentStats.extractors

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.col
import util.{Extractor, MongoConnector}

object UserActivityExtractor extends Extractor{

    override def extract(startTime: Long, endTime: Long): DataFrame = {
        MongoConnector
        .loadCollection[UserActivity]("user_activity")
        .filter(col("timestamp") >= startTime && col("timestamp") <= endTime)
    }
}

case class UserActivity(sessionId: String, userAction: String, timestamp: Long)