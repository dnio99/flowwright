package com.dnio.flowwright.api.routes.workflow.v1

import com.dnio.flowwright.api.interop.core._
import com.dnio.flowwright.api.layer.AllEnv
import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.execute.WorkflowExecute
import com.dnio.flowwright.core.parser.WorkflowParser
import com.dnio.flowwright.core.workflow.OriginalWorkflow
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import sttp.tapir.json.circe._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.ztapir._
import zio.ZIO

object WorkflowRoutes {

  final case class In(
      workflow: Option[OriginalWorkflow],
      config: Option[OriginalWorkflow],
      data: Option[Map[String, Json]]
  ) derives Encoder.AsObject,
        Decoder

  private[v1] val runEndpoint = BASE.post
    .in("run")
    .in(jsonBody[Json])
    .out(jsonBody[Json])
    .summary("Run Workflow")
    .zServerLogic(json => {
      (for {
        in <- ZIO
          .fromEither(json.as[In])
          .mapError(e =>
            WorkflowErrors
              .WorkflowParseError("Invalid input", Some(e.getMessage))
          )

        originalWorkflow <- ZIO
          .fromOption(in.config.orElse(in.workflow))
          .orElseFail(
            WorkflowErrors
              .WorkflowParseError("Workflow configuration is required")
          )

        workflow <- ZIO.fromEither(
          WorkflowParser.parse(
            originalWorkflow
          )
        )
        res <- WorkflowExecute.execute(
          workflow,
          in.data.getOrElse(Map.empty)
        )

      } yield res).mapError(
        _.toApiError
      )
    })

  private[v1] val allEndpoints = List(
    runEndpoint.widen[AllEnv]
  )
}
