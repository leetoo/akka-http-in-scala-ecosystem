package com.epam

import akka.http.scaladsl.server.Directives._


class Source18  {

  val route1 =
    path("a") {
      get {
        complete("get a")
      } ~
      post {
        complete("post a")
      }
    } ~
    get {
      path("b") {
        complete("get b")
      }
    }

  val route2 = path("a") {
    get {
      complete("get a")
    } ~         //TODO: Don't forget ~
      post {
        complete("post a")
      }
  }

  val route3 = path("a") {
    get {
      complete {
        println("get a sub-route")
        "get a"
      }
    } ~
      post {
        println("post a sub-route")
        complete {
          "post a"
        }
      }
  }

}
