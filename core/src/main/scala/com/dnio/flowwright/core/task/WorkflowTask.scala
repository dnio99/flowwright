package com.dnio.flowwright.core.task

import com.dnio.flowwright.core.node.NodeId
import io.circe.Json
import zio.Ref

final case class WorkflowTask(
    id: NodeId,
    status: TaskStatus = TaskStatus.Pending(),
    result: Option[Json] = None
)

type WorkflowTaskState = Ref[Map[NodeId, WorkflowTask]]
