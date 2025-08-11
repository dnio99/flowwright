package com.dnio.flowwright.core.node.http_request

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.interop.shared._
import com.dnio.flowwright.core.node.WorkflowNodeBody
import com.dnio.flowwright.core.template.TemplateResolver
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import com.dnio.shared.http4s.syntax._
import com.dnio.shared.http4s.zio_interop.ZioHttp4sClient
import com.dnio.shared.http4s.zio_interop.ZioHttp4sClient.given
import io.circe.Decoder.given
import io.circe.Json
import org.http4s.Method
import org.http4s.Request
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import zio.Task
import zio.ZIO

final case class OriginalHttpRequestBody(
    method: Option[String],
    url: String,
    headers: Option[Map[String, String]] = None,
    body: Option[Json] = None,
    bodyType: Option[String],
    postProcessExpression: Option[String] = None
)

final case class HttpRequestBody(
    method: Method,
    url: String,
    headers: Map[String, String],
    body: Json,
    bodyType: HttpRequestBody.BodyType,
    postProcessExpression: Option[String]
) extends WorkflowNodeBody {

  type T = HttpRequestBody

  override def resolver(
      data: WorkflowContextData
  ): ZIO[JmespathZio.Service, WorkflowError, HttpRequestBody] =
    for {
      url <- TemplateResolver.handle[String, String](data, this.url)
      headers <- TemplateResolver
        .handle[Map[String, String], Map[String, String]](
          data,
          this.headers
        )
      body <- TemplateResolver.handle[Json, Json](data, this.body)
    } yield this.copy(
      url = url,
      headers = headers,
      body = body
    )

  override type R = Client[Task] & JmespathZio.Service

  override def run(
      data: WorkflowContextData
  ): ZIO[R, WorkflowError, Json] = {
    for {
      body <- resolver(data)

      uri <- ZIO
        .fromEither(body.url.uri)
        .mapError(e =>
          WorkflowErrors.WorkflowNodeExecutionError(
            "Invalid URL in HttpRequestNode",
            Some(e.message)
          )
        )

      // todo: body type

      res <- ZioHttp4sClient
        .request[Json](
          Request[Task](
            method = method,
            uri = uri
          ).putHeaders(
            headers.map { case (k, v) =>
              (k, v)
            }.toSeq
          ).withEntity(
            body.body
          )
        )
        .mapError(_.toWorkflowError)
    } yield res

  }
}

object HttpRequestBody {

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

  def fromOriginal(original: OriginalHttpRequestBody): HttpRequestBody = {
    HttpRequestBody(
      method = original.method
        .flatMap(Method.fromString(_).toOption)
        .getOrElse(Method.GET),
      url = original.url,
      headers = original.headers.getOrElse(Map.empty),
      body = original.body.getOrElse(Json.Null),
      bodyType = BodyType.fromString(original.bodyType),
      postProcessExpression = original.postProcessExpression
    )

  }

}
