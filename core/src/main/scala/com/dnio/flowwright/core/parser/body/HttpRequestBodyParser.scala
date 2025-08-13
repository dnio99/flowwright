package com.dnio.flowwright.core.parser.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.NodeKind
import com.dnio.flowwright.core.node.http_request.HttpRequestBody
import com.dnio.flowwright.core.node.http_request.OriginalHttpRequestBody
import io.circe.Json

object HttpRequestBodyParser extends BodyParser[HttpRequestBody] {

  val nodeKind: NodeKind = NodeKind.HttpRequest

  override def parse(
      json: Json
  ): Either[WorkflowErrors.WorkflowError, HttpRequestBody] = {
    json
      .as[OriginalHttpRequestBody]
      .map(
        HttpRequestBody.fromOriginal
      )
      .left
      .map(e =>
        WorkflowErrors.WorkflowNodeParseError(
          "Failed to parse HttpRequestBody",
          Some(e.getMessage)
        )
      )
  }
}
