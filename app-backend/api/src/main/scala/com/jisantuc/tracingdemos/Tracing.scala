package com.jisantuc.tracingdemos.api

import cats.effect.IO
import com.colisweb.tracing._
import com.colisweb.tracing.TracingContext.TracingContextBuilder
import io.opentracing._

object Tracing {
  val tracingContextBuilder: TracingContextBuilder[IO] =
    LoggingTracingContext[IO] _
}
