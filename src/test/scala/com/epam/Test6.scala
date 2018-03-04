package com.epam

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import spray.json.DefaultJsonProtocol

class Test6 extends org.scalatest.FunSuite with org.scalatest.Matchers with ScalatestRouteTest {

  // domain model
  final case class Item(name: String, id: Long)

  // Step 1 and step 2 in one sencence
  trait JsonSupport extends DefaultJsonProtocol {
    implicit val itemFormat = jsonFormat2(Item)
  }

  class MyJsonService extends JsonSupport with SprayJsonSupport {
    val route =
      get {
        pathSingleSlash {
          complete(Item("thing", 42)) // will render as JSON
        }
      }
  }

  val service = new MyJsonService()

  test("JSON (un)marshalling") {
    Get() ~> service.route ~> check {
      status should ===(StatusCodes.OK)
      responseAs[String] should ===("""{"name":"thing","id":42}""")
    }
  }


}
