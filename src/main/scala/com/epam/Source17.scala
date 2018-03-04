package com.epam

import java.nio.file.{ Path, Paths }

import scala.util.{ Failure, Success }
import scala.concurrent.Future

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import akka.http.scaladsl.model.Multipart.FormData
import akka.http.scaladsl.marshalling.Marshal


class Source17  {

  implicit val system = ActorSystem()
  import system.dispatcher // to get an implicit ExecutionContext into scope
  implicit val materializer = ActorMaterializer()

  case class FileToUpload(name: String, location: Path)

  def filesToUpload(): Source[FileToUpload, NotUsed] =
  // This could even be a lazy/infinite stream. For this example we have a finite one:
    Source(List(
      FileToUpload("foo.txt", Paths.get("./foo.txt")),
      FileToUpload("bar.txt", Paths.get("./bar.txt")),
      FileToUpload("baz.txt", Paths.get("./baz.txt"))
    ))

  val poolClientFlow =
    Http().cachedHostConnectionPool[FileToUpload]("akka.io")

  def createUploadRequest(fileToUpload: FileToUpload): Future[(HttpRequest, FileToUpload)] = {
    val bodyPart =
    // fromPath will use FileIO.fromPath to stream the data from the file directly
      FormData.BodyPart.fromPath(fileToUpload.name, ContentTypes.`application/octet-stream`, fileToUpload.location)

    val body = FormData(bodyPart) // only one file per upload
    Marshal(body).to[RequestEntity].map { entity => // use marshalling to create multipart/formdata entity
      // build the request and annotate it with the original metadata
      HttpRequest(method = HttpMethods.POST, uri = "http://example.com/uploader", entity = entity) -> fileToUpload
    }
  }

  // you need to supply the list of files to upload as a Source[...]
  filesToUpload()
    // The stream will "pull out" these requests when capacity is available.
    // When that is the case we create one request concurrently
    // (the pipeline will still allow multiple requests running at the same time)
    .mapAsync(1)(createUploadRequest)
    // then dispatch the request to the connection pool
    .via(poolClientFlow)
    // report each response
    // Note: responses will not come in in the same order as requests. The requests will be run on one of the
    // multiple pooled connections and may thus "overtake" each other.
    .runForeach {
    case (Success(response), fileToUpload) =>
      println(s"Result for file: $fileToUpload was successful: $response")
      response.discardEntityBytes() // don't forget this
    case (Failure(ex), fileToUpload) =>
      println(s"Uploading file $fileToUpload failed with $ex")
  }

}
