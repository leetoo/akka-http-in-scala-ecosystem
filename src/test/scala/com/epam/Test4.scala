package com.epam

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.{ContentTypes, MediaRange, MediaTypes}
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Source

class Test4 extends org.scalatest.FunSuite with org.scalatest.Matchers with ScalatestRouteTest {

  case class Tweet(uid: Int, txt: String)
  case class Measurement(id: String, value: Int)

  //1) marshalling infrestructure
  object MyJsonProtocol
    extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
      with spray.json.DefaultJsonProtocol {

    implicit val tweetFormat = jsonFormat2(Tweet.apply)
    implicit val measurementFormat = jsonFormat2(Measurement.apply)
  }

//  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
//    EntityStreamingSupport.json()
//      .withParallelMarshalling(parallelism = 8, unordered = false)

  //2) import "my protocol", for unmarshalling Measurement objects
  import MyJsonProtocol._

  //3) enable Json Streaming
  // Note that the default support renders the Source as JSON Array
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()

  def getTweets: Source[Tweet, NotUsed] = Source.apply(List(
    Tweet(1, "#Akka rocks!"),
    Tweet(2, "Streaming is so hot right now!"),
    Tweet(3, "You cannot enter the same river twice.")
  ))


  val route =
    path("tweets") {
      complete(getTweets)
    }

  // tests ------------------------------------------------------------
  val AcceptJson = Accept(MediaRange(MediaTypes.`application/json`))
  val AcceptXml = Accept(MediaRange(MediaTypes.`text/xml`))

  test("Send stream") {
    Get("/tweets").withHeaders(AcceptJson) ~> route ~> check {
      responseAs[String] shouldEqual
        """[""" +
          """{"uid":1,"txt":"#Akka rocks!"},""" +
          """{"uid":2,"txt":"Streaming is so hot right now!"},""" +
          """{"uid":3,"txt":"You cannot enter the same river twice."}""" +
          """]"""
    }

    // endpoint can only marshal Json, so it will *reject* requests for application/xml:
    Get("/tweets").withHeaders(AcceptXml) ~> route ~> check {
      handled should ===(false)
      rejection should ===(UnacceptedResponseContentTypeRejection(Set(ContentTypes.`application/json`)))
    }
  }

}
