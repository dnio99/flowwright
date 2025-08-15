package com.dnio.flowwright.api.layer

import cats.effect.Async
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.client.RequestKey
import org.http4s.ember.client.EmberClientBuilder
import zio.Task
import zio.ZLayer
import zio.interop.catz._

import scala.concurrent.duration.DurationInt

object Http4sClientLayer {

  val live: ZLayer[Any, Throwable, Client[Task]] =
    ZLayer.scoped {
      val async = Async.apply[Task]
      EmberClientBuilder
        .default(using async, Network.forAsync(using async))
        .withTimeout(
          45.seconds
        )
        .withMaxTotal(
          3072
        )
        .withMaxPerKey((_: RequestKey) => 1024)
        .build
        .toScopedZIO
    }

}
