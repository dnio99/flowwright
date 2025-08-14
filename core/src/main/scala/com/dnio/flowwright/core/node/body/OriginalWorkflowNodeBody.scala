package com.dnio.flowwright.core.node.body

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.body.WorkflowNodeBody

trait OriginalWorkflowNodeBody {

  def toWorkflowNodeBody: Either[WorkflowError, WorkflowNodeBody[?, ?]]

}
