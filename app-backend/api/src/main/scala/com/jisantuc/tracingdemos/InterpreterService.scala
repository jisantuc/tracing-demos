package com.jisantuc.tracingdemos.api

import com.jisantuc.tracingdemos.datamodel._
import ALBTracedHttpRoutes._

import cats.effect._
import com.colisweb.tracing.TracingContext.TracingContextBuilder
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class InterpreterService(implicit tracingContext: TracingContextBuilder[IO]) {
  object NOpsQueryParamMatcher extends QueryParamDecoderMatcher[Int]("ops")
  object SeedQueryParamMatcher extends QueryParamDecoderMatcher[Int]("seed")

  // allOpsForever is an infinite stream of op constructors
  // we'll take some number of items from it
  val allOpsForever: fs2.Stream[IO, Op => Op] = fs2.Stream
    .emits(Op.shuffledOpsConstructors)
    .covary[IO] ++ allOpsForever

  def routes: HttpRoutes[IO] = ALBTracedHttpRoutes[IO] {
    case GET -> Root :? NOpsQueryParamMatcher(ops) :? SeedQueryParamMatcher(seed) using tracingContext =>
      for {
        opConstructors <- tracingContext.childSpan("collect-ops", Map("nOps" -> s"$ops")) use { _ =>
          allOpsForever.take(ops).compile.to[List]
        }
        op <- tracingContext.childSpan("build-op", Map("seed" -> s"$seed")) use { _ =>
          IO {
            opConstructors.foldRight(Lit(seed): Op)((f: Op => Op, x: Op) => f(x))
          }
        }
        result <- IO(Interpreter(op)).attempt
        resp <- result match {
          case Right(res) =>
            tracingContext.childSpan("send-response",
                                     Map("result" -> s"$result", "responseType" -> "success")) use {
              _ =>
                Ok(
                  Map("result" -> res.asJson, "operations" -> op.asJson).asJson
                )
            }
          case Left(err) =>
            tracingContext.childSpan(
              "send-response",
              Map("result" -> err.getMessage, "responseType" -> "failure")) use { _ =>
              BadRequest(Map("err" -> err.getMessage.asJson, "operations" -> op.asJson).asJson)
            }
        }
      } yield resp
  }
}
