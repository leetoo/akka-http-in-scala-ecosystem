package com.epam

import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import StatusCodes._
import Directives._


class Source19  {

  implicit def myRejectionHandler =
    RejectionHandler.newBuilder()
      .handle { case MissingCookieRejection(cookieName) =>
        complete(HttpResponse(BadRequest, entity = "No cookies, no service!!!"))
      }
      .handleAll[MethodRejection] { methodRejections =>
        val names = methodRejections.map(_.supported.name)
        complete((MethodNotAllowed, s"Can't do that! Supported: ${names mkString " or "}!"))
      }
      .handleNotFound {
        complete((NotFound, "Not here!"))
      }
      .result()

}
