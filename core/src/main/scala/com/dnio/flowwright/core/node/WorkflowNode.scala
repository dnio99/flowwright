package com.dnio.flowwright.core.node

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.task.WorkflowTaskState
import com.dnio.flowwright.core.workflow.WorkflowContextData
import net.reactivecore.cjs.DocumentValidator
import zio.ZIO

opaque type NodeId = String

trait WorkflowNode {
  val id: NodeId
  val name: String
  val description: Option[String]
  val dependentOn: Seq[NodeId]
  val body: WorkflowNodeBody
  val inputValidator: DocumentValidator
  val outputValidator: DocumentValidator
  val onFailure: Option[WorkflowNode] = None

  type R

  def execute(
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState
  ): ZIO[R, WorkflowError, Unit]

}
