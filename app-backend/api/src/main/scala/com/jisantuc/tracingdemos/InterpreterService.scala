package com.jisantuc.tracingdemos.api

import com.jisantuc.tracingdemos.datamodel._

import cats.effect._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class InterpreterService() {
  object NOpsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("ops")
  object SeedQueryParamMatcher extends QueryParamDecoderMatcher[Int]("seed")

  // allOpsForever is an infinite stream of op constructors
  // we'll take some number of items from it
  val allOpsForever: fs2.Stream[IO, Op => Op] = fs2.Stream
    .emits(Op.shuffledOpsConstructors)
    .covary[IO] ++ allOpsForever

  val routes: HttpRoutes[IO] = HttpRoutes.of {
    case GET -> Root :? NOpsQueryParamMatcher(ops) :? SeedQueryParamMatcher(seed) =>
      for {
        opConstructors <- allOpsForever.take(ops).compile.to[List]
        op = opConstructors.foldRight(Lit(seed): Op)((f: Op => Op, x: Op) => f(x))
        result <- IO(Interpreter(op)).attempt
        resp <- result match {
          case Right(res) =>
            Ok(
              Map("result" -> res.asJson, "operations" -> op.asJson).asJson
            )
          case Left(err) =>
            BadRequest(Map("err" -> err.getMessage.asJson, "operations" -> op.asJson).asJson)
        }
      } yield resp
  }
}
