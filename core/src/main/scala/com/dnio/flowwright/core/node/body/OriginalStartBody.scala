package com.dnio.flowwright.core.node.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.body.WorkflowNodeBody.StartBody
import io.circe.Json

final case class OriginalStartBody(
    input: Option[Map[String, Json]],
    postProcessExpression: Option[String] = None
) extends OriginalWorkflowNodeBody {

  override def toWorkflowNodeBody
      : Either[WorkflowErrors.WorkflowError, WorkflowNodeBody[?, ?]] = Right(
    StartBody(
      input = input.getOrElse(Map.empty),
      postProcessExpression = postProcessExpression
    )
  )
}
