package com.dnio.shared.http4s

import cats.effect.Async
import com.dnio.shared.http4s.Http4sClient.LogLevel
import org.http4s.Request
import org.http4s.Response
import org.http4s.client.Client
import org.http4s.client.middleware.RequestLogger
import org.http4s.client.middleware.ResponseLogger

private object LoggerClient {

  def apply[F[_]: Async](logAction: Option[String => F[Unit]])(
      client: Client[F]
  )(using loggerConfig: Http4sClient.LoggerConfig): Client[F] = {
    ResponseLogger.apply(
      logBody = loggerConfig.logResponseBody,
      logAction = logAction,
      logHeaders = loggerConfig.logResponseHeaders
    )(
      RequestLogger.apply(
        logBody = loggerConfig.logRequestBody,
        logAction = logAction,
        logHeaders = loggerConfig.logRequestHeaders
      )(client)
    )
  }
}

object Http4sClient {

  enum LogLevel:
    case Trace, Debug, Info

  final case class LoggerConfig(
      logRequestBody: Boolean,
      logResponseBody: Boolean,
      logRequestHeaders: Boolean,
      logResponseHeaders: Boolean,
      logLevel: Option[LogLevel]
  )

  object LoggerConfig {

    val default: LoggerConfig = LoggerConfig(
      false,
      false,
      false,
      false,
      None
    )
  }

}
trait Http4sClient[F[_]: Async] {

  protected def logAction(logLevel: Option[LogLevel]): Option[String => F[Unit]]

  protected def withLogging(
      client: Client[F]
  )(using
      loggerConfig: Http4sClient.LoggerConfig,
      logAction: Option[String => F[Unit]]
  ): Client[F] = {
    loggerConfig.logLevel match {
      case Some(value) => LoggerClient(logAction)(client)
      case None        => client
    }
  }

  protected def onError(
      req: Request[F]
  )(
      res: Response[F]
  ): F[Throwable]

}
