package util

object BatchProcessor {

  private var batches: Map[String, Batch] = Map();

  def subscribeBatch(name: String, batch: Batch) = {
    batches += (name -> batch);
  }

  def processBatch(name: String) = {
    val batch = batches(name);
    if (batch == null) throw new Exception(s"$name batch does not exist");

    val dataFrames = batch.getData();
    if (dataFrames == null)
      throw new Exception(s"$name batch was not able to extract source data");

    val result = batch.transform(dataFrames);
    if (result == null)
      throw new Exception(s"$name batch was not able to transform data");

    batch.store(result);
  }
}
