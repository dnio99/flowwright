package com.dnio.flowwright.core.node.body

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.body.WorkflowNodeBody.EndBody
import io.circe.Decoder

case class OriginalEndBody(
    output: Map[String, String],
    postProcessExpression: Option[String]
) extends OriginalWorkflowNodeBody derives Decoder {
  override def toWorkflowNodeBody
      : Either[WorkflowErrors.WorkflowError, EndBody] = Right(
    EndBody(
      output = output,
      postProcessExpression = postProcessExpression
    )
  )
}
