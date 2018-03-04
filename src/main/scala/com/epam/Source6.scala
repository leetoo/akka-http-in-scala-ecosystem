package com.epam

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future

object Source6 extends App{

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future foreach in the end
  implicit val executionContext = system.dispatcher

  //dummy failure monitor
  val failureMonitor = system.actorOf(Props())

  val reactToTopLevelFailures = Flow[IncomingConnection]
    .watchTermination()((_, termination) => termination.failed.foreach {
      cause => failureMonitor ! cause
    })

  val reactToConnectionFailure = Flow[HttpRequest]
    .recover[HttpRequest] {
    case ex =>
      // handle the failure somehow
      throw ex
  }

  val (host, port) = ("localhost", 80)
  val serverSource = Http().bind(host, port)

  val httpEcho = Flow[HttpRequest]
    .via(reactToConnectionFailure)  //3) Connections failures
    .map { request =>
      HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, request.entity.dataBytes))
    }

  val bindingFuture: Future[Done] = serverSource
    .via(reactToTopLevelFailures) //2) Connection Source failures
    .runForeach { con =>
      con.handleWith(httpEcho)
    }

  //1) Handling bind failures
  bindingFuture.failed.foreach { ex =>
    println(s"Failed to bind to $host:$port!")
  }

}
