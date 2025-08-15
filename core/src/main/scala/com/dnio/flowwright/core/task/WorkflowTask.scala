package com.dnio.flowwright.core.task

import com.dnio.flowwright.core.node.NodeId
import io.circe.Json
import zio.Ref

import java.time.Instant

final case class WorkflowTask(
    id: NodeId,
    status: TaskStatus = TaskStatus.Pending(),
    result: Option[Json] = None,
    createdAt: Instant = Instant.now(),
    updatedAt: Instant = Instant.now(),
    version: Int = 0
)

type WorkflowTaskState = Ref[Map[NodeId, WorkflowTask]]
