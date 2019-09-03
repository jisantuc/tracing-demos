package com.jisantuc.tracingdemos.api

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import io.jaegertracing.Configuration._
import natchez.{EntryPoint, Span, Trace}
import natchez.jaeger.Jaeger
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.middleware._
import org.http4s.server.Router

object ApiServer extends IOApp {

  implicit def concKleisliSpan[F[_]: ConcurrentEffect: Trace]
    : ConcurrentEffect[Kleisli[F, Span[F], ?]] =
    new ConcurrentEffect[Kleisli[F, Span[F], ?]] {

      def runCancelable[A](fa: F[A])(cb: Either[Throwable, A] => IO[Unit]): SyncIO[CancelToken[F]] =
        ???
    }

  def httpApp[F[_]: Trace: Sync]: HttpApp[F] =
    CORS(
      Router(
        "/api/hello"       -> new HelloService[F]().routes,
        "/api/interpreter" -> new InterpreterService[F]().routes
      )).orNotFound

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] =
    Jaeger.entryPoint[F]("natchez-example") { c =>
      Sync[F].delay {
        c.withSampler(SamplerConfiguration.fromEnv)
          .withReporter(ReporterConfiguration.fromEnv)
          .getTracer
      }
    }

  def runF[F[_]: Trace: Concurrent: ContextShift: Timer: ConcurrentEffect]: F[Unit] =
    Trace[F].span("session") {
      BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
    }

  def run(args: List[String]): IO[ExitCode] =
    entryPoint[IO].use { ep =>
      ep.root("root").use { span =>
        runF[Kleisli[IO, Span[IO], ?]].run(span)
      }
    } as ExitCode.Success
}
