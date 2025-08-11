package com.dnio.flowwright.core.task

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError

sealed trait TaskStatus

object TaskStatus {

  final case class Pending() extends TaskStatus

  final case class Running() extends TaskStatus

  final case class Succeeded() extends TaskStatus

  final case class Failed(workFlowError: WorkflowError) extends TaskStatus

  final case class Cancelled() extends TaskStatus

}
