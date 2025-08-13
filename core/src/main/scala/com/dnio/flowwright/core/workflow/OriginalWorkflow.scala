package com.dnio.flowwright.core.workflow

import com.dnio.flowwright.core.node.OriginalWorkflowNode

final case class OriginalWorkflow(
    id: WorkflowId,
    name: String,
    description: Option[String],
    nodes: Seq[OriginalWorkflowNode]
)
