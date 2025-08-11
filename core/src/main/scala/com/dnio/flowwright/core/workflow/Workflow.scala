package com.dnio.flowwright.core.workflow

import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.WorkflowNode
import io.circe.Json
import zio.Ref

final case class Workflow(
    id: String,
    name: String,
    description: Option[String],
    nodes: Map[NodeId, WorkflowNode]
)

type WorkflowContextData = Ref[Map[String, Json]]
