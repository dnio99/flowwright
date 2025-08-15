package com.dnio.flowwright.api

import cats.implicits._
import com.dnio.flowwright.api.layer.AllEnv
import com.dnio.flowwright.api.routes.SystemRoutes
import com.dnio.flowwright.api.routes.workflow
import org.http4s.HttpRoutes
import sttp.apispec.openapi.Info
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.RIO
import zio.interop.catz._

package object application {

  private val swaggerRoutes: HttpRoutes[RIO[AllEnv, *]] =
    ZHttp4sServerInterpreter()
      .from(
        SwaggerInterpreter().fromServerEndpoints(
          SystemRoutes.AllEndpoint ::: workflow.v1.AllEndpoints,
          Info(
            title = BuildInfo.name,
            version = BuildInfo.version
          )
        )
      )
      .toRoutes

  private[application] val AllRoutes: HttpRoutes[RIO[AllEnv, *]] =
    SystemRoutes.AllRoutes <+> workflow.v1.AllRoutes <+> swaggerRoutes

}
