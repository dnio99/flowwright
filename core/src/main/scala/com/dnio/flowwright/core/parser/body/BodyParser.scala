package com.dnio.flowwright.core.parser.body
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.NodeKind
import com.dnio.flowwright.core.node.WorkflowNodeBody
import io.circe.Json

trait BodyParser[T <: WorkflowNodeBody] {

  val nodeKind: NodeKind

  def parse(json: Json): Either[WorkflowError, T]

}

object BodyParser {

  def parse(
      nodeKind: NodeKind,
      json: Json
  ): Either[WorkflowError, WorkflowNodeBody] = {
    nodeKind match {
      case NodeKind.HttpRequest => HttpRequestBodyParser.parse(json)
    }
  }
}
