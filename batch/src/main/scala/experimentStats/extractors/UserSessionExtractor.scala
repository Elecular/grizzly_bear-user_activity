package experimentStats.extractors

import org.apache.spark.sql.DataFrame
import util.{Extractor, MongoConnector}
import org.apache.spark.sql.functions._

object UserSessionExtractor extends Extractor{

    /**
      * Gets all user session within given time range
      */
    override def extract(startTime: Long, endTime: Long): DataFrame = {
        MongoConnector
        .loadCollection[UserSession]("user_session")
        .filter(col("timestamp") >= startTime && col("timestamp") <= endTime)
    }
}

case class UserSession(_id: String,
                       hourNumber: Int,
                       projectId: String,
                       segments: Array[String],
                       timestamp: Long,
                       userId: String,
                       environment: String)