package com.dnio.flowwright.api.routes

import com.dnio.flowwright.api.BuildInfo
import com.dnio.flowwright.api.layer.AllEnv
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.RIO
import zio.ZIO

object SystemRoutes {

  private val systemVersionEndpoint: ZServerEndpoint[Any, Any] = BASE_ROUTES.get
    .in("system")
    .in("version")
    .out(stringBody)
    .summary("get system version")
    .zServerLogic(_ => ZIO.succeed(BuildInfo.toString))

  val AllEndpoint = List(
    systemVersionEndpoint.widen[AllEnv]
  )

  val AllRoutes: HttpRoutes[RIO[AllEnv, *]] =
    ZHttp4sServerInterpreter().from(AllEndpoint).toRoutes

}
