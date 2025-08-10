package com.dnio.shared.http4s.zio_interop

import com.dnio.shared.http4s.Http4sClient
import com.dnio.shared.http4s.Http4sClient.LogLevel
import com.dnio.shared.http4s.errors.Http4sErrors
import com.dnio.shared.http4s.errors.Http4sErrors.ErrorInfo
import com.dnio.shared.http4s.errors.Http4sErrors.UnexpectedResponse
import io.circe.Decoder
import io.circe.parser._
import org.http4s.EntityDecoder
import org.http4s.InvalidMessageBodyFailure
import org.http4s.ParseFailure
import org.http4s.Request
import org.http4s.Response
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import zio.Task
import zio.ZIO
import zio.interop.catz.asyncInstance
import zio.stream.ZStream
import zio.stream.interop.fs2z.fs2RIOStreamSyntax

object ZioHttp4sClient extends Http4sClient[Task] {

  object EntityDecoderInstant {
    given EntityDecoder[Task, Unit] =
      EntityDecoder.void[Task]

    given EntityDecoder[Task, String] =
      EntityDecoder.text[Task]

  }

  given Http4sClient.LoggerConfig = Http4sClient.LoggerConfig.default

  given circeEntityDecoder[T](using Decoder[T]): EntityDecoder[Task, T] =
    jsonOf[Task, T]

  override protected def logAction(
      logLevel: Option[Http4sClient.LogLevel]
  ): Option[String => Task[Unit]] = {
    logLevel.map {
      case LogLevel.Trace => ZIO.logTrace(_)
      case LogLevel.Debug => ZIO.logDebug(_)
      case LogLevel.Info  => ZIO.logInfo(_)
    }
  }

  def request[T](req: Request[Task])(using
      loggerConfig: Http4sClient.LoggerConfig,
      entityDecoder: EntityDecoder[Task, T]
  ): ZIO[Client[Task], Http4sErrors.Http4sError, T] = {
    given Option[String => Task[Unit]] = logAction(loggerConfig.logLevel)
    for {
      client <- ZIO.service[Client[Task]].map(withLogging)

      res <- client.expectOr[T](req)(onError(req)).mapError {
        case e: InvalidMessageBodyFailure =>
          Http4sErrors.DecodeFailure(e.getMessage())

        case e: ParseFailure => Http4sErrors.DecodeFailure(e.getMessage())

        case e: UnexpectedResponse => e
        case e: Throwable          => Http4sErrors.UnknownFailure(e.getMessage)
      }

    } yield res
  }

  override protected def onError(
      req: Request[Task]
  )(response: Response[Task]): Task[Throwable] =
    for {
      requestBody <- req.bodyText.toZStream().orElse(ZStream.empty).runHead

      responseBody <- response.bodyText
        .toZStream()
        .orElse(ZStream.empty)
        .runHead

      errorInfo = responseBody.flatMap(
        decode[ErrorInfo](_).toOption
      )

    } yield UnexpectedResponse(
      method = req.method.name,
      status = response.status.code,
      uri = req.uri.toString(),
      headers = req.headers.headers.map(r => r.name.toString -> r.value).toMap,
      body = requestBody,
      responseBody = responseBody,
      errorInfo = errorInfo
    )

}
