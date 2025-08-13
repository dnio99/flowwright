package com.dnio.flowwright.core.errors

object WorkflowErrors {

  trait WorkflowError {}

  final case class TemplateParseError(
      message: String = "TemplateParseError!",
      description: Option[String] = None
  ) extends WorkflowError

  final case class WorkflowNodeNotFoundError(
      message: String = "WorkflowNodeNotFoundError!",
      description: Option[String] = None
  ) extends WorkflowError

  final case class WorkflowNodeAbnormalError(
      message: String = "WorkflowNodeAbnormalError!",
      description: Option[String] = None
  ) extends WorkflowError

  final case class WorkflowNodeExecutionError(
      message: String = "WorkflowNodeExecutionError!",
      description: Option[String] = None,
      code: Int = 500
  ) extends WorkflowError

  final case class WorkflowHttp4sError(
      message: String = "WorkflowHttp4sError!",
      description: Option[String] = None,
      code: Int
  ) extends WorkflowError

  final case class WorkflowJmespathError(
      message: String = "WorkflowJmespathError!",
      description: Option[String] = None
  ) extends WorkflowError

  final case class WorkflowNodeValidationError(
      message: String = "WorkflowNodeValidationError!",
      description: Option[String] = None
  ) extends WorkflowError

  final case class WorkflowNodeParseError(
      message: String = "WorkflowNodeParseError!",
      description: Option[String] = None
  ) extends WorkflowError

  final case class WorkflowBodyParserNotFoundError(
      message: String = "WorkflowBodyParserNotFoundError!",
      description: Option[String] = None
  ) extends WorkflowError
}
