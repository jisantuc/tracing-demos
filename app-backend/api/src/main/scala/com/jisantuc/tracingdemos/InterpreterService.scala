package com.jisantuc.tracingdemos.api

import com.jisantuc.tracingdemos.datamodel._

import cats.effect._
import cats.implicits._
import fs2.{Pure, Stream}
import io.circe.syntax._
import natchez._
import org.http4s._
import org.http4s.dsl.io._

import java.util.Base64

class InterpreterService[F[_]: Trace: Sync]() {
  object NOpsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("ops")
  object SeedQueryParamMatcher extends QueryParamDecoderMatcher[Int]("seed")

  // allOpsForever is an infinite stream of op constructors
  // we'll take some number of items from it
  def allOpsForever: Stream[Pure, Op => Op] =
    Stream
      .emits(Op.shuffledOpsConstructors) ++ allOpsForever

  def routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? NOpsQueryParamMatcher(ops) :? SeedQueryParamMatcher(seed) =>
      Trace[F].span("interpret-expr") {
        for {
          _ <- Trace[F].put("n-ops" -> ops, "seed" -> seed)
          opConstructors = allOpsForever.take(ops).compile.to[List]
          op             = opConstructors.foldRight(Lit(seed): Op)((f: Op => Op, x: Op) => f(x))
          _ <- Trace[F].put("interpret-tree" -> op.asJson.noSpaces)
          result = Either.catchNonFatal(Interpreter(op))
          resp <- result match {
            case Right(res) =>
              Trace[F].put("approx-result" -> res.toInt) map { _ =>
                Response(
                  body = Stream
                    .emits(
                      Base64
                        .getEncoder()
                        .encode(
                          Map("result" -> res.asJson, "operations" -> op.asJson).asJson.noSpaces.getBytes
                        )
                    )
                    .covary[F]
                )
              }
            case Left(err) =>
              Trace[F].put("err" -> err.getMessage) map { _ =>
                Response(
                  status = Status.BadRequest,
                  body = Stream
                    .emits(
                      Base64
                        .getEncoder()
                        .encode(
                          Map("err" -> err.getMessage.asJson, "operations" -> op.asJson).asJson.noSpaces.getBytes)
                    )
                    .covary[F]
                )
              }
          }
        } yield resp
      }
  }
}
