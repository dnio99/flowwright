package com.dnio.flowwright.api.application

import cats.data.Kleisli
import cats.effect.kernel.Async
import com.comcast.ip4s._
import com.dnio.flowwright.api.layer.AllEnv
import com.dnio.flowwright.api.layer.all
import fs2.io.net.Network
import org.http4s.Request
import org.http4s.Response
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import zio._
import zio.interop.catz._

object Loader extends ZIOAppDefault {

  val httpApp: Kleisli[RIO[AllEnv, *], Request[RIO[AllEnv, *]], Response[
    RIO[AllEnv, *]
  ]] = Router("/" -> AllRoutes).orNotFound

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] = {
    val async = Async.apply[RIO[AllEnv, *]]
    EmberServerBuilder
      .default(using async, Network.forAsync(using async))
      .withPort(Port.fromInt(9999).get)
      .withHost(Host.fromString("0.0.0.0").get)
      .withHttpApp(httpApp)
      .build
      .useForever
      .exitCode
      .unit
      .provideLayer(all)

  }

}
