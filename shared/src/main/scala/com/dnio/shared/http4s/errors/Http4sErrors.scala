package com.dnio.shared.http4s.errors

import io.circe.Decoder
import io.circe.Encoder

import scala.util.control.NoStackTrace

object Http4sErrors {

  final case class ErrorInfo(
      message: Option[String],
      description: Option[String]
  ) derives Encoder.AsObject,
        Decoder

  sealed trait Http4sError extends Throwable {
    val message: String
  }

  final case class ParseUriFailure(message: String) extends Http4sError

  final case class UnexpectedResponse(
      method: String,
      status: Int,
      uri: String,
      headers: Map[String, String],
      body: Option[String],
      responseBody: Option[String],
      errorInfo: Option[ErrorInfo]
  ) extends RuntimeException
      with Http4sError
      with NoStackTrace {

    override val message: String =
      s"""
         |method: ${method}
         |uri: ${uri}
         |headers: ${headers}
         |status: ${status}
         |responseBody: ${responseBody}
         |""".stripMargin

  }

  final case class DecodeFailure(message: String) extends Http4sError

  final case class UnknownFailure(message: String) extends Http4sError
}
