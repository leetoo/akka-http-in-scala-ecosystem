package com.epam

import akka.actor.ActorSystem

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}

import scala.concurrent.ExecutionContext.Implicits.global

class Test2 extends org.scalatest.FunSuite with org.scalatest.Matchers {

  test("Explicit marshalling") {

    val string = "Yeah"
    val entityFuture: Future[MessageEntity] = Marshal(string).to[MessageEntity]
    val entity = Await.result(entityFuture, 1.second) // don't block in non-test code!
    entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

    val errorMsg = "Easy, pal!"
    val responseFuture = Marshal(420 -> errorMsg).to[HttpResponse]
    val response = Await.result(responseFuture, 1.second) // don't block in non-test code!
    response.status shouldEqual StatusCodes.EnhanceYourCalm
    response.entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

    val request = HttpRequest(headers = List(headers.Accept(MediaTypes.`application/json`)))
    val responseText = "Plaintext"
    val respFuture = Marshal(responseText).toResponseFor(request) // with content negotiation!
    a[Marshal.UnacceptableResponseContentTypeException] should be thrownBy {
      Await.result(respFuture, 1.second) // client requested JSON, we only have text/plain!
    }

  }

  test("Explicit unmarshalling") {

    implicit val system: ActorSystem = ActorSystem("defaultActorSystem")
    implicit val materializer: Materializer = ActorMaterializer()   //we need Materializer to unmarshal

    val intFuture = Unmarshal("42").to[Int]
    val int = Await.result(intFuture, 1.second) // don't block in non-test code!
    int shouldEqual 42

    val boolFuture = Unmarshal("off").to[Boolean]
    val bool = Await.result(boolFuture, 1.second) // don't block in non-test code!
    bool shouldBe false

  }

}
