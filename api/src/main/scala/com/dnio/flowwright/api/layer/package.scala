package com.dnio.flowwright.api

import com.dnio.jmespath.JmespathZio
import org.http4s.client.Client
import zio.Task
import zio.ZLayer

package object layer {

  type AllEnv = Client[Task] & JmespathZio.Service

  val all: ZLayer[Any, Throwable, AllEnv] = ZLayer.make[AllEnv](
    JmespathZio.live,
    Http4sClientLayer.live,
    LoggingLayer.live
  )

}
