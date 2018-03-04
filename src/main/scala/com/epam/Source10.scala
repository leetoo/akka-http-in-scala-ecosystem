package com.epam

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.io.StdIn

class Source10  {

  def innerRoute(id: Int): Route =
    get {
      complete {
        "Received GET request for order " + id
      }
    } ~
      put {
        complete {
          "Received PUT request for order " + id
        }
      }

  val route1: Route = path("order" / IntNumber) { id => innerRoute(id) }



  val route2 =
    path("order" / IntNumber) { id =>
      (get | put) { ctx =>
        ctx.complete(s"Received ${ctx.request.method.name} request for order $id")
      }
    }



  val getOrPut = get | put  //because getOrPut doesn’t take any parameters, it can be a val here.
  val route3 =
    path("order" / IntNumber) { id =>
      getOrPut {
        extractMethod { m =>
          complete(s"Received ${m.name} request for order $id")
        }
      }
    }



  val route4 =
    (path("order" / IntNumber) & getOrPut & extractMethod) { (id, m) =>   //concatenation of its sub-extractions
      complete(s"Received ${m.name} request for order $id")
    }



  val orderGetOrPutWithMethod = path("order" / IntNumber) & (get | put) & extractMethod
  val route5 =
    orderGetOrPutWithMethod { (id, m) =>
      complete(s"Received ${m.name} request for order $id")
    }



  def innerRouteConcatCombined(id: Int): Route = //concat() method using
    concat(get {
      complete {
        "Received GET request for order " + id
      }
    },
      put {
        complete {
          "Received PUT request for order " + id
        }
      })

  val route: Route = path("order" / IntNumber) { id => innerRouteConcatCombined(id) }

}
