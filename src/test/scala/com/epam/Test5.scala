package com.epam

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Flow

import scala.concurrent.Future

class Test5 extends org.scalatest.FunSuite with org.scalatest.Matchers with ScalatestRouteTest {

  case class Tweet(uid: Int, txt: String)
  case class Measurement(id: String, value: Int)

  //1) marshalling infrestructure
  object MyJsonProtocol
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
      with spray.json.DefaultJsonProtocol {

    implicit val tweetFormat = jsonFormat2(Tweet.apply)
    implicit val measurementFormat = jsonFormat2(Measurement.apply)
  }

  //2) import "my protocol", for unmarshalling Measurement objects
  import MyJsonProtocol._

  //3) enable Json Streaming
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  // prepare your persisting logic here
  val persistMetrics = Flow[Measurement]

  val route =
    path("metrics") {
      // [3] extract Source[Measurement, _]
      entity(asSourceOf[Measurement]) { measurements =>
        // alternative syntax:
        // entity(as[Source[Measurement, NotUsed]]) { measurements =>
        val measurementsSubmitted: Future[Int] =
          measurements
            .via(persistMetrics)
            .runFold(0) { (cnt, _) => cnt + 1 }

        complete {
          measurementsSubmitted.map(n => Map("msg" -> s"""Total metrics received: $n"""))
        }
      }
    }


  test("Receive stream") {

    val data = HttpEntity(
      ContentTypes.`application/json`,
      """
        |{"id":"temp","value":32}
        |{"id":"temp","value":31}
        |
  """.stripMargin)

    Post.apply("/metrics", entity = data) ~> route ~> check {
      status should ===(StatusCodes.OK)
      responseAs[String] should ===("""{"msg":"Total metrics received: 2"}""")
    }

    // the FramingWithContentType will reject any content type that it does not understand:
    val xmlData = HttpEntity(
      ContentTypes.`text/xml(UTF-8)`,
      """|<data id="temp" value="32"/>
         |<data id="temp" value="31"/>""".stripMargin)

    Post.apply("/metrics", entity = xmlData) ~> route ~> check {
      handled should ===(false)
      rejection should ===(UnsupportedRequestContentTypeRejection(Set(ContentTypes.`application/json`)))
    }

  }

}
