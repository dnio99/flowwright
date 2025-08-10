package com.dnio.flowwright.core.node.http_request

import io.circe.Json
import org.http4s.Method

final case class OriginalHttpRequestNode(
    method: Option[String],
    url: String,
    headers: Option[Map[String, String]] = None,
    body: Option[Json] = None,
    bodyType: Option[String]
)

final case class HttpRequestNode(
    method: Method,
    url: String,
    headers: Map[String, String],
    body: Json,
    bodyType: HttpRequestNode.BodyType
)

object HttpRequestNode {

  protected enum BodyType:
    private case JsonBody, FormBody, TextBody, NoBody;

  private object BodyType {
    def fromString(value: Option[String]): BodyType = value
      .map { v =>
        v.toUpperCase() match {
          case "JSON" => JsonBody
          case "FORM" => FormBody
          case "TEXT" => TextBody
          case _      => NoBody
        }
      }
      .getOrElse(NoBody)
  }

  def fromOriginal(original: OriginalHttpRequestNode): HttpRequestNode = {
    HttpRequestNode(
      method = original.method
        .flatMap(Method.fromString(_).toOption)
        .getOrElse(Method.GET),
      url = original.url,
      headers = original.headers.getOrElse(Map.empty),
      body = original.body.getOrElse(Json.Null),
      bodyType = BodyType.fromString(original.bodyType)
    )

  }

}
