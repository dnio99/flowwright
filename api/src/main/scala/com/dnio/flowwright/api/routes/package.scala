package com.dnio.flowwright.api

import com.dnio.flowwright.api.errors.ApiErrors._
import sttp.model.StatusCode
import sttp.tapir.Endpoint
import sttp.tapir.EndpointOutput
import sttp.tapir.generic.auto.schemaForCaseClass
import sttp.tapir.json.circe._
import sttp.tapir.ztapir._

package object routes {

  private val errorInfoEndpointOutput
      : EndpointOutput.OneOf[ApiError, ApiError] = oneOf[ApiError](
    oneOfVariant(
      StatusCode.NotFound,
      jsonBody[NotFound].description("NotFound")
    ),
    oneOfVariant(
      StatusCode.BadRequest,
      jsonBody[BadRequest].description("BadRequest")
    ),
    oneOfVariant(
      StatusCode.InternalServerError,
      jsonBody[InternalServerError].description("internal server error")
    ),
    oneOfVariant(
      StatusCode.Forbidden,
      jsonBody[Forbidden].description("Forbidden")
    ),
    oneOfVariant(
      StatusCode.PreconditionRequired,
      jsonBody[PreconditionRequired].description("Precondition Required")
    ),
    oneOfVariant(
      StatusCode.TooManyRequests,
      jsonBody[TooManyRequests].description("rate limit exceeded")
    ),
    oneOfVariant(
      StatusCode.Conflict,
      jsonBody[Conflict].description("Conflict")
    ),
    oneOfVariant(
      StatusCode.NotAcceptable,
      jsonBody[NotAcceptable].description("NotAcceptable")
    ),
    oneOfVariant(
      StatusCode.NotFound,
      jsonBody[NotFound].description("NotFound")
    ),
    oneOfVariant(
      StatusCode.Unauthorized,
      jsonBody[Unauthorized].description("Unauthorized")
    ),
    oneOfVariant(
      StatusCode.Locked,
      jsonBody[Locked].description("Locked")
    )
  )

  protected[routes] val BASE_ROUTES: Endpoint[Unit, Unit, ApiError, Unit, Any] =
    endpoint
      .errorOut(
        errorInfoEndpointOutput
      )

}
