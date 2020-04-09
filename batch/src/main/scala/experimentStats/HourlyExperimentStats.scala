package experimentStats

import org.apache.spark.sql.DataFrame;
import com.mongodb.spark.sql.fieldTypes.Timestamp
import util.Batch
import util.MongoConnector
import org.apache.spark.sql.functions.explode


case class UserSession(
    _id: String,
    hourNumber: Int,
    projectId: String,
    segments: Array[String],
    timestamp: Timestamp,
    userId: String
)

case class UserActivity(
    sessionId: String,
    userAction: String
)

/**
  * Used for computing hourly results for experiment events
  */
object HourlyExperimentStats extends Batch("HourlyExperimentStats") {

  override def getData(): Map[String, DataFrame] = {
    ExperimentsExtractor.getRunningExperiments(79839129600000L, 79839302400000L);
    return Map(
      "UserSession" -> MongoConnector.loadCollection[UserSession](
        "user_session"
      ),
      "UserActivity" -> MongoConnector.loadCollection[UserActivity](
        "user_activity"
      )
    )
  }

  override def transform(dataFrames: Map[String, DataFrame]): DataFrame = {
    //val userSession = dataFrames("UserSession")
    //val userActivity = dataFrames("UserActivity").groupBy("sessionId","userAction").count();

    //userSession.
    //  join(userActivity, userSession("_id") === userActivity("sessionId"), "inner").
    //  show(20, false);
    //dataFrames("UserActivity").show(20, false);
    return null;
  }

  override def store(dataFrame: DataFrame) = {}

}
