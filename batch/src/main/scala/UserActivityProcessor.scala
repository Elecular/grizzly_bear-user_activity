import util._
import experimentStats._
import monthlyActiveUsers.MonthlyActiveUsers
import dailyPerformanceMetrics.DailyPerformanceMetrics

object UserActivityProcessor {

    def main(args: Array[String]) {
        AppSparkSession
        initBatches()

        if (args.length == 0)
            throw new Exception("No Arguments Passed")
        else if(args.length == 1)
            BatchProcessor.processBatch(args(0))
        else if(args.length == 3)
            BatchProcessor.processBatchForTimeRange(args(0), args(1).toLong, args(2).toLong)
        else
            throw new Exception("Invalid Arguments passed")
    }

    def initBatches(): Unit = {
        HourlyExperimentStats
        MonthlyActiveUsers
        DailyPerformanceMetrics
    }

}
