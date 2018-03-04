package com.epam

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

class Source12  {

  object MyApp extends App {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()

    case class Color(red: Int, green: Int, blue: Int)

    val route1: Route =
      path("color") {
        parameters('red.as[Int], 'green.as[Int], 'blue.as[Int]) { (red, green, blue) =>
          val color = Color(red, green, blue)
          complete("OK")
        }
      }


    val route2 =
      path("color") {
        parameters('red.as[Int], 'green.as[Int], 'blue.as[Int]).as(Color) { color =>
          complete("OK")
        }
      }


    case class Color2(red: Int, green: Int, blue: Int)
    object Color2 {
      //any companion object
    }

    val route3 =
      path("color") {
        parameters('red.as[Int], 'green.as[Int], 'blue.as[Int]).as(Color2.apply) { color =>
          complete("OK")
        }
      }


    case class Color3(red: Int, green: Int, blue: Int) {
      require(0 <= red && red <= 255, "red color component must be between 0 and 255")
      require(0 <= green && green <= 255, "green color component must be between 0 and 255")
      require(0 <= blue && blue <= 255, "blue color component must be between 0 and 255")
    }

    val route4 =
      path("color") {
        parameters('red.as[Int], 'green.as[Int], 'blue.as[Int]).as(Color3) { color => // may generate ValidationRejections
          complete("OK")
        }
      }


  }


}
