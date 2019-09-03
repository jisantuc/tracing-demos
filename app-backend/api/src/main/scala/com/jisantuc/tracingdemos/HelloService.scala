package com.jisantuc.tracingdemos.api

import cats.effect._
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class HelloService[F[_]: Sync] extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / name => {
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
    }
  }
}
