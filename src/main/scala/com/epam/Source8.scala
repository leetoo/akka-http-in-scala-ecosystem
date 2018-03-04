package com.epam

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{HttpApp, Route}

// Server definition
object WebServer extends HttpApp {
  override def routes: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }
}

object Source8 extends App {

  // Starting the server
  WebServer.startServer("localhost", 8080)

}

