package com.epam

import akka.http.scaladsl.model._

class Source2 {

  import StatusCodes._

  // simple OK response without data created using the integer status code
  HttpResponse(200)

  // 404 response created using the named StatusCode constant
  HttpResponse(NotFound)

  // 404 response with a body explaining the error
  HttpResponse(404, entity = "Unfortunately, the resource couldn't be found.")

  // A redirecting response containing an extra header
  val locationHeader = headers.Location("http://example.com/other")
  HttpResponse(Found, headers = List(locationHeader))

}
