package com.dnio.flowwright.core.node.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.interop.jmespath._
import com.dnio.flowwright.core.interop.shared._
import com.dnio.flowwright.core.template.TemplateResolver
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import com.dnio.shared.http4s.syntax._
import com.dnio.shared.http4s.zio_interop.ZioHttp4sClient
import com.dnio.shared.http4s.zio_interop.ZioHttp4sClient.given
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import org.http4s.Method
import org.http4s.Request
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import zio.Ref
import zio.Task
import zio.ZIO

sealed trait WorkflowNodeBody[R, T] {

  val postProcessExpression: Option[String]

  def resolver(
      data: Ref[Map[String, Json]]
  ): ZIO[JmespathZio.Service, WorkflowError, T]

  protected def logic(
      data: WorkflowContextData
  ): ZIO[R, WorkflowError, Json]

  def run(
      data: WorkflowContextData
  ): ZIO[R & JmespathZio.Service, WorkflowError, Json] = {
    for {
      originalRes <- logic(data)
      res <- (originalRes, postProcessExpression) match {
        case (json, Some(expression)) if !json.isNull => {
          for {
            jmespath <- ZIO.service[JmespathZio.Service]
            res <- jmespath
              .search(json, expression)
              .mapError(
                _.toWorkflowError
              )
          } yield res
        }
        case _ => ZIO.succeed(originalRes)
      }
    } yield res
  }
}

object WorkflowNodeBody {

  final case class EndBody(
      output: Map[String, String],
      postProcessExpression: Option[String]
  ) extends WorkflowNodeBody[JmespathZio.Service, EndBody] {

    override def resolver(data: Ref[Map[String, Json]]): ZIO[
      JmespathZio.Service,
      WorkflowErrors.WorkflowError,
      EndBody
    ] = ZIO.succeed(this)

    override protected def logic(
        data: WorkflowContextData
    ): ZIO[JmespathZio.Service, WorkflowErrors.WorkflowError, Json] = for {
      body <- resolver(data)

      res <- TemplateResolver.handle[Map[String, Json], Map[String, String]](
        data,
        body.output
      )

    } yield res.asJson
  }

  final case class HttpRequestBody(
      method: Method,
      url: String,
      headers: Map[String, String],
      body: Json,
      bodyType: HttpRequestBody.BodyType,
      postProcessExpression: Option[String]
  ) extends WorkflowNodeBody[
        Client[Task] & JmespathZio.Service,
        HttpRequestBody
      ] {

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

    override def logic(
        data: WorkflowContextData
    ): ZIO[Client[Task] & JmespathZio.Service, WorkflowError, Json] = {
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

    enum BodyType:
      private case JsonBody, FormBody, TextBody, NoBody;

    object BodyType {
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
  }

}
