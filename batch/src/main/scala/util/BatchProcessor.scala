package util

object BatchProcessor {

    private var batches: Map[String, Batch] = Map();

    def subscribeBatch(name: String, batch: Batch): Unit = {
        batches += (name -> batch)
    }

    def processBatch(name: String): Unit = {
        val batch = batches(name)
        if (batch == null) throw new Exception(s"$name batch does not exist");
        batch.run()

    }

    def processBatchForTimeRange(name: String, startTime: Long, endTime: Long): Unit = {
        val batch = batches(name)
        if (batch == null) throw new Exception(s"$name batch does not exist");
        batch.execute(startTime, endTime)
    }
}
