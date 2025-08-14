package com.dnio.flowwright.core.node.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.body.WorkflowNodeBody.HttpRequestBody
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import org.http4s.Method

final case class OriginalHttpRequestBody(
    method: Option[String],
    url: String,
    headers: Option[Map[String, String]] = None,
    body: Option[Json] = None,
    bodyType: Option[String],
    postProcessExpression: Option[String] = None
) extends OriginalWorkflowNodeBody derives Encoder.AsObject, Decoder {

  override def toWorkflowNodeBody
      : Either[WorkflowErrors.WorkflowError, HttpRequestBody] =
    Right(
      HttpRequestBody(
        method = method
          .map(_.toUpperCase())
          .flatMap(Method.fromString(_).toOption)
          .getOrElse(Method.GET),
        url = url,
        headers = headers.getOrElse(Map.empty),
        body = body.getOrElse(Json.Null),
        bodyType = HttpRequestBody.BodyType.fromString(bodyType),
        postProcessExpression = postProcessExpression
      )
    )
}
