package experimentStats

import scalaj.http.Http

object ExperimentsExtractor {

    private val host = sys.env("PRIVATE_EXPERIMENTS_SERVICE_HOST");
    private val port = sys.env("PRIVATE_EXPERIMENTS_SERVICE_PORT");

    def getRunningExperiments(startTime: Long, endTime: Long) = {
        val experiments = Http(s"http://$host:$port/experiment/timerange/$startTime/$endTime").asString;
        println(experiments.body);
    }
}