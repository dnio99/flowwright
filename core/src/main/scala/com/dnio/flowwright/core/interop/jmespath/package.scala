package com.dnio.flowwright.core.interop

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.jmespath.errors.JmespathError

package object jmespath {

  extension (jmespathError: JmespathError) {
    def toWorkflowError: WorkflowError = {
      WorkflowErrors.WorkflowJmespathError(
        message = jmespathError.message,
        description = jmespathError.description
      )
    }
  }
}
