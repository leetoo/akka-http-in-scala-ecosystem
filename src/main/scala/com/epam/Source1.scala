package com.epam

import akka.http.scaladsl.model._
import akka.util.ByteString

class Source1 {

  import HttpMethods._

  // construct a simple GET request to `homeUri`
  val homeUri = Uri("/abc")
  HttpRequest(GET, uri = homeUri)

  // construct simple GET request to "/index" (implicit string to Uri conversion)
  HttpRequest(GET, uri = "/index")

  // construct simple POST request containing entity
  val data = ByteString("abc")
  HttpRequest(POST, uri = "/receive", entity = data)

  // customize every detail of HTTP request
  import HttpCharsets._
  import HttpProtocols._
  import MediaTypes._
  val userData = ByteString("abc")
  val authorization = headers.Authorization(headers.BasicHttpCredentials("user", "pass"))
  HttpRequest(
    PUT,
    uri = "/user",
    entity = HttpEntity(`text/plain` withCharset `UTF-8`, userData),
    headers = List(authorization),
    protocol = `HTTP/1.0`)

}
