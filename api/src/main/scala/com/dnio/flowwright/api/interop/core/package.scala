package com.dnio.flowwright.api.interop

import com.dnio.flowwright.api.errors.ApiErrors
import com.dnio.flowwright.api.errors.ApiErrors.ApiError
import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError

package object core {

  extension (workflowError: WorkflowError) {
    def toApiError: ApiError = {
      workflowError match {
        case WorkflowErrors.WorkflowBodyParserNotFoundError(
              message,
              description
            ) =>
          ApiErrors.NotFound(message, description)
        case WorkflowErrors.WorkflowCycleError(message, description) =>
          ApiErrors.BadRequest(message, description)
        case WorkflowErrors.WorkflowHttp4sError(message, description, code) => {
          code match {
            case 400 => ApiErrors.BadRequest(message, description)
            case 401 => ApiErrors.Unauthorized(message, description)
            case 403 => ApiErrors.Forbidden(message, description)
            case 404 => ApiErrors.NotFound(message, description)
            case 408 => ApiErrors.RequestTimeout(message, description)
            case _   => ApiErrors.InternalServerError(message, description)
          }
        }
        case WorkflowErrors.WorkflowNodeNotFoundError(message, description) =>
          ApiErrors.NotFound(message, description)
        case WorkflowErrors.WorkflowNodeValidationError(message, description) =>
          ApiErrors.BadRequest(message, description)
        case _ =>
          ApiErrors.InternalServerError(
            message = workflowError.message,
            description = workflowError.description
          )
      }
    }
  }

}
