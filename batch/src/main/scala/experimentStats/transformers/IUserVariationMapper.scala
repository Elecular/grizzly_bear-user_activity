package experimentStats.transformers

import org.apache.spark.sql.{DataFrame, Dataset, Row}

/**
  * This class is used for getting the variations assigned to a user.
  * This is used in ExperimentSessions to map user to variations
  */
abstract class IUserVariationMapper {

    /**
      * This method takes a dataset of
      *     projectId
      *     experimentId
      *     userId
      * and returns back the following Data frame
      *     projectId
      *     experimentId
      *     userId
      *     variationName
      * It returns the variation the user was assigned to.
      */
    def getVariationForUser(experimentToUserId: Dataset[Row]): DataFrame
}
