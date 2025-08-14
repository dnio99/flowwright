package com.dnio.flowwright.core.task

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError

sealed trait TaskStatus(val isSuccess: Boolean, val isFailed: Boolean) {}

object TaskStatus {

  final case class Pending()
      extends TaskStatus(isSuccess = false, isFailed = false)

  final case class Waiting()
      extends TaskStatus(isSuccess = false, isFailed = false)

  final case class Running()
      extends TaskStatus(isSuccess = false, isFailed = false)

  final case class Succeeded()
      extends TaskStatus(isSuccess = true, isFailed = false)

  final case class Skipped()
      extends TaskStatus(isSuccess = true, isFailed = false)

  final case class Failed(workFlowError: WorkflowError)
      extends TaskStatus(isSuccess = false, isFailed = true)

  final case class Cancelled()
      extends TaskStatus(isSuccess = false, isFailed = true)

}
