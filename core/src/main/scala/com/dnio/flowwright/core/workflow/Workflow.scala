package com.dnio.flowwright.core.workflow
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.node.WorkflowNode.EndNode
import io.circe.Json
import zio.Ref

opaque type WorkflowId = String

object WorkflowId {

  def apply(id: String): WorkflowId = id

  extension (workflowId: WorkflowId) def asString: String = workflowId
}

final case class Workflow(
    id: WorkflowId,
    name: String,
    description: Option[String],
    nodes: Seq[WorkflowNode],
    endNode: EndNode,
    leafNodeIds: Set[NodeId],
    childrenNodes: Map[NodeId, Seq[WorkflowNode]]
) {

  val getRootNodes: Seq[WorkflowNode] = nodes.filter(_.dependentOn.isEmpty)

}

type WorkflowContextData = Ref[Map[String, Json]]
