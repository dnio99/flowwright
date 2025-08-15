package com.dnio.flowwright.api.routes.workflow

import com.dnio.flowwright.api.layer.AllEnv
import com.dnio.flowwright.api.routes.BASE_ROUTES
import com.dnio.flowwright.api.routes.workflow.v1.WorkflowRoutes.allEndpoints
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir._
import zio.RIO

package object v1 {

  private[v1] val BASE = BASE_ROUTES
    .in("api" / "workflows" / "v1")
    .tags(List("Workflow v1"))

  val AllEndpoints = allEndpoints

  val AllRoutes: HttpRoutes[RIO[AllEnv, *]] =
    ZHttp4sServerInterpreter().from(AllEndpoints).toRoutes

}
