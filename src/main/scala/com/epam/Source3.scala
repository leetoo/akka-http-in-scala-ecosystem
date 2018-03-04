package com.epam

import akka.http.scaladsl.model._

class Source3 {

  import akka.http.scaladsl.model.headers._

  // create a ``Location`` header
  val loc = Location("http://example.com/other")

  // create an ``Authorization`` header with HTTP Basic authentication data
  val auth = Authorization(BasicHttpCredentials("joe", "josepp"))

  // custom type
  case class User(name: String, pass: String)

  // a method that extracts basic HTTP credentials from a request
  def credentialsOfRequest(req: HttpRequest): Option[User] =
    for {
      Authorization(BasicHttpCredentials(user, pass)) <- req.header[Authorization]
    } yield User(user, pass)

}
