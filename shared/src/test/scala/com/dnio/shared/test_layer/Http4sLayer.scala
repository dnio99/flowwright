package com.dnio.shared.test_layer

import cats.effect.Async
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import zio.interop.catz.*
import zio.{Task, ZLayer}

object Http4sLayer {

  val live: ZLayer[Any, Throwable, Client[Task]] =
    ZLayer.scoped {
      val async = Async.apply[Task]
      EmberClientBuilder
        .default(using async, Network.forAsync(using async))
        .build
        .toScopedZIO
    }

}
