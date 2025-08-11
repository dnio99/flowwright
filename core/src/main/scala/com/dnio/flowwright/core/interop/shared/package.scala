package com.dnio.flowwright.core.interop

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.shared.http4s.errors.Http4sErrors
package object shared {

  extension (http4sError: Http4sErrors.Http4sError) {
    def toWorkflowError: WorkflowError = {
      http4sError match {
        case Http4sErrors.ParseUriFailure(message) =>
          WorkflowErrors.WorkflowHttp4sError(
            message = message,
            description = Some("Failed to parse URI"),
            code = 400 // You can set a specific error code here
          )
        case Http4sErrors.UnexpectedResponse(
              method,
              status,
              uri,
              headers,
              body,
              responseBody,
              errorInfo
            ) =>
          WorkflowErrors.WorkflowHttp4sError(
            message =
              s"Unexpected response for method: $method, status: $status, uri: $uri",
            description = errorInfo.flatMap(_.message),
            code = status
          )
        case Http4sErrors.DecodeFailure(message) =>
          WorkflowErrors.WorkflowHttp4sError(
            message = message,
            description =
              Some("Decode failure occurred while processing HTTP response"),
            code = 500 // You can set a specific error code here
          )
        case Http4sErrors.UnknownFailure(message) =>
          WorkflowErrors.WorkflowHttp4sError(
            message = message,
            description =
              Some("An unknown error occurred while processing HTTP response"),
            code = 500 // You can set a specific error code here
          )
      }
    }

  }

}
