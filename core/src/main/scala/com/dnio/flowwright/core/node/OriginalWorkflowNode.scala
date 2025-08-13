package com.dnio.flowwright.core.node

import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json

final case class OriginalWorkflowNode(
    id: String,
    name: String,
    description: Option[String],
    dependentOn: Option[Seq[String]],
    body: Json,
    inputValidator: Option[Json],
    outputValidator: Option[Json],
    onFailure: Option[Json] = None,
    kind: NodeKind
) derives Encoder.AsObject,
      Decoder
