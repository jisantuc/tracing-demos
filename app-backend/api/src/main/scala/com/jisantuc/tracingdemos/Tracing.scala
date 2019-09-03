package com.jisantuc.tracingdemos.api

import cats.effect.{ContextShift, IO, Timer}
import com.colisweb.tracing._
import com.colisweb.tracing.TracingContext.{TracingContextBuilder, TracingContextResource}

object Tracing {

  def tracingContextBuilder(implicit contextShift: ContextShift[IO],
                            timer: Timer[IO]): TracingContextBuilder[IO] =
    new TracingContextBuilder[IO] {

      def apply(operationName: String,
                tags: Map[String, String] = Map.empty): TracingContextResource[IO] = {
        LoggingTracingContext[IO]()(operationName, tags)
      }
    }
}
