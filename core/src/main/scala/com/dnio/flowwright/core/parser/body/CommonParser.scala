package com.dnio.flowwright.core.parser.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.body.OriginalWorkflowNodeBody
import com.dnio.flowwright.core.node.body.WorkflowNodeBody
import io.circe.Decoder
import io.circe.Json

final case class CommonParser[T <: OriginalWorkflowNodeBody]()
    extends BodyParser[T] {

  override def parse(
      json: Json
  )(using Decoder[T]): Either[WorkflowError, WorkflowNodeBody[?, ?]] = {
    json
      .as[T]
      .left
      .map(e =>
        WorkflowErrors
          .WorkflowNodeParseError("Failed to parse body", Some(e.getMessage))
      )
      .flatMap(_.toWorkflowNodeBody)
  }
}
