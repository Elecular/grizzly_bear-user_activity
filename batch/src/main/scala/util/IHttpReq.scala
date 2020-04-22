package util

/**
  * Used for making http requests
  */
trait IHttpReq {
    def request(url: String, postData: String): String
}
