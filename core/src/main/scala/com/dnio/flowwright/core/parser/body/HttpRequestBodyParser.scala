package com.dnio.flowwright.core.parser.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.body.OriginalHttpRequestBody
import com.dnio.flowwright.core.node.body.WorkflowNodeBody
import io.circe.Decoder
import io.circe.Json

object HttpRequestBodyParser extends BodyParser[OriginalHttpRequestBody] {

  override def parse(
      json: Json
  )(using
      Decoder[OriginalHttpRequestBody]
  ): Either[WorkflowErrors.WorkflowError, WorkflowNodeBody[?, ?]] = {
    json
      .as[OriginalHttpRequestBody]
      .left
      .map(e =>
        WorkflowErrors.WorkflowNodeParseError(
          "Failed to parse HttpRequestBody",
          Some(e.getMessage)
        )
      )
      .flatMap(_.toWorkflowNodeBody)
  }
}
