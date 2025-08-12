package com.dnio.shared.test_layer
import zio.Runtime
import zio.ZLayer
import zio.logging.backend.SLF4J
object LoggingLayer {

  val live: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

}
