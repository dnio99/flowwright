package com.dnio.flowwright.core.errors

object WorkFlowErrors {

  trait WorkFlowError {}

  final case class TemplateParseError(
      message: String = "TemplateParseError!",
      description: Option[String] = None
  ) extends WorkFlowError

}
