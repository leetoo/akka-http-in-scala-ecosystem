package com.epam

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

class Test3 extends org.scalatest.FunSuite with org.scalatest.Matchers with ScalatestRouteTest {

  val smallRoute =
    get {
      pathSingleSlash {
        complete {
          "Captain on the bridge!"
        }
      } ~
        path("ping") {
          complete("PONG!")
        }
    }


  test("The service should return a greeting for GET requests to the root path") {
    // tests:
    Get() ~> smallRoute ~> check {
      responseAs[String] shouldEqual "Captain on the bridge!"
    }
  }

    test("The service should return a 'PONG!' response for GET requests to /ping") {
    // tests:
    Get("/ping") ~> smallRoute ~> check {
      responseAs[String] shouldEqual "PONG!"
    }
  }

    test("The service should leave GET requests to other paths unhandled") {
    // tests:
    Get("/kermit") ~> smallRoute ~> check {
      handled shouldBe false
    }
  }

    test("The service should return a MethodNotAllowed error for PUT requests to the root path") {
    // tests:
    Put() ~> Route.seal(smallRoute) ~> check {
      status shouldEqual StatusCodes.MethodNotAllowed
      responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
    }
  }

}
