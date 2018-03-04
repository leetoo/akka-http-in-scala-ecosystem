package com.epam

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import akka.stream.ActorMaterializer

class Source11  {

  val localExceptionHandler = ExceptionHandler {
    case _: ArithmeticException =>
      extractUri { uri =>
        println(s"Request to $uri could not be handled normally")
        complete(HttpResponse(InternalServerError, entity = "Bad numbers, bad result!!!"))
      }
  }

  implicit def newDefaultExceptionHandler: ExceptionHandler = //implicit ExceptionHandler will be used instead of default
    ExceptionHandler {
      case _: ArithmeticException =>
        extractUri { uri =>
          println(s"Request to $uri could not be handled normally")
          complete(HttpResponse(InternalServerError, entity = "Bad numbers, bad result!!!"))
        }
    }

  object MyApp extends App {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val route: Route =
      handleExceptions(localExceptionHandler) { //specified ExceptionHandler will be used for this sub-route
        pathSingleSlash {
         complete("OK")
        }
      }

    Http().bindAndHandle(route, "localhost", 8080)
  }


}
