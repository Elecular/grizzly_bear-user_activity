package experimentStats

import scalaj.http.Http

object ExperimentsExtractor {

    private val host = sys.env("EXPERIMENTS_DEPLOYMENT_SERVICE_SERVICE_HOST");
    private val port = sys.env("EXPERIMENTS_DEPLOYMENT_SERVICE_SERVICE_PORT");

    def getRunningExperiments(startTime: Long, endTime: Long) = {
        val experiments = Http(s"http://$host:$port/experiment/timerange/$startTime/$endTime").asString;
        println(experiments.body);
    }
}