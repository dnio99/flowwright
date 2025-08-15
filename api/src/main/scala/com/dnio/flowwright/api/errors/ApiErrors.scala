package com.dnio.flowwright.api.errors

import io.circe.Decoder
import io.circe.Encoder

object ApiErrors {

  sealed trait ApiError {
    val message: String
    val description: Option[String]
  }

  final case class RequestTimeout(
      message: String = "RequestTimeout!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class BadRequest(
      message: String = "BadRequest!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class Unauthorized(
      message: String = "Unauthorized!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class Forbidden(
      message: String = "Internal Server Error!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class NotFound(
      message: String = "Not Found!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class Conflict(
      message: String = "Conflict",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class NotAcceptable(
      message: String = "Not Acceptable",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class Locked(
      message: String = "Locked!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class PreconditionRequired(
      message: String = "Precondition Required!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class TooManyRequests(
      message: String = "Too Many Requests!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

  final case class InternalServerError(
      message: String = "Internal Server Error!",
      description: Option[String] = None
  ) extends ApiError derives Encoder.AsObject, Decoder

}
