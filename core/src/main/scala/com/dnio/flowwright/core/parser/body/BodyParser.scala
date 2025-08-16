package com.dnio.flowwright.core.parser.body
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.NodeKind
import com.dnio.flowwright.core.node.body.OriginalEndBody
import com.dnio.flowwright.core.node.body.OriginalStartBody
import com.dnio.flowwright.core.node.body.OriginalWorkflowNodeBody
import com.dnio.flowwright.core.node.body.WorkflowNodeBody
import io.circe.Decoder
import io.circe.Json

trait BodyParser[T <: OriginalWorkflowNodeBody] {

  def parse(json: Json)(using
      Decoder[T]
  ): Either[WorkflowError, WorkflowNodeBody[?, ?]]

}

object BodyParser {

  def parse(
      nodeKind: NodeKind,
      json: Json
  ): Either[WorkflowError, WorkflowNodeBody[?, ?]] = {
    nodeKind match {
      case NodeKind.HttpRequest => HttpRequestBodyParser.parse(json)
      case NodeKind.Start       => CommonParser[OriginalStartBody]().parse(json)
      case NodeKind.End         => CommonParser[OriginalEndBody]().parse(json)
    }
  }
}
