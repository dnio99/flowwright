package com.dnio.jmespath.errors

import io.circe.{Decoder, Encoder}

sealed trait JmespathError {
  val message: String
  val description: Option[String]
}

object JmespathError {

  final case class JsonParseError(
      message: String = "JsonParseError!",
      description: Option[String] = None
  ) extends JmespathError derives Encoder.AsObject, Decoder

  final case class ExpressionParseError(
      message: String = "ExpressionParseError!",
      description: Option[String] = None
  ) extends JmespathError derives Encoder.AsObject, Decoder

  final case class SearchError(
      message: String = "SearchError!",
      description: Option[String] = None
  ) extends JmespathError derives Encoder.AsObject, Decoder

}
