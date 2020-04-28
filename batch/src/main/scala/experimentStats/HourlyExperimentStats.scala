package experimentStats

import experimentStats.extractors.{RunningExperimentsExtractor, UserActivityExtractor, UserSessionExtractor}
import experimentStats.transformers.{ExperimentActivityStats, ExperimentSessionStats, ExperimentSessions}
import util.{AggregationInterval, Batch, MongoConnector}
import org.apache.spark.sql.functions.col

/**
  * Used for computing hourly results for experiment events
  */
object HourlyExperimentStats extends Batch("HourlyExperimentStats", AggregationInterval.Hourly, 3) {

    override def execute(startTime: Long, endTime: Long): Unit = {

        val userSessions = UserSessionExtractor.extract(startTime, endTime)
        val experiments = RunningExperimentsExtractor.extract(startTime, endTime)

        //We want all user activities that happened in the next 3 hours
        val userActivity = UserActivityExtractor.extract(startTime, endTime + (3600 * 1000 * 3))

        val experimentSessions = ExperimentSessions.transform(Map(
            "userSessions" ->  userSessions,
            "experiments" -> experiments
        ))

        experimentSessions.cache

        val experimentSessionStats = ExperimentSessionStats.transform(Map(
            "experimentSessions" -> experimentSessions
        ))

        val experimentActivityStats = ExperimentActivityStats.transform(Map(
            "experimentSessions" -> experimentSessions,
            "userActivity" -> userActivity
        ))

        MongoConnector.writeToCollection("experiment_session_stats", experimentSessionStats);
        MongoConnector.writeToCollection("experiment_activity_stats", experimentActivityStats)
    }
}