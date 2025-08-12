package com.dnio.flowwright.core.node

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.task.WorkflowTaskState
import com.dnio.flowwright.core.workflow.WorkflowContextData
import net.reactivecore.cjs.DocumentValidator
import zio.ZIO

opaque type NodeId = String

object NodeId {

  def apply(id: String): NodeId = id

  extension (nodeId: NodeId) def asString: String = nodeId
}

trait WorkflowNode {
  val id: NodeId
  val name: String
  val description: Option[String]
  val dependentOn: Seq[NodeId]
  val body: WorkflowNodeBody
  val inputValidator: Option[DocumentValidator]
  val outputValidator: Option[DocumentValidator]
  val onFailure: Option[WorkflowNode] = None

  type R

  def execute(
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState
  ): ZIO[R, WorkflowError, Unit]

}
