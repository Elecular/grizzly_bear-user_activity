package util

import scalaj.http.Http

/**
  * Used for making http requests
  */
class HttpReq extends IHttpReq {
    override def request(url: String, postData: String): String = {
        Http(url)
        .postData(postData)
        .header("Content-type", "application/json")
        .asString.body
    }
}
