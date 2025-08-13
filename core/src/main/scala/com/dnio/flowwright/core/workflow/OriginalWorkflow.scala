package com.dnio.flowwright.core.workflow

import com.dnio.flowwright.core.node.OriginalWorkflowNode
import io.circe.Decoder
import io.circe.Encoder

final case class OriginalWorkflow(
    id: String,
    name: String,
    description: Option[String],
    nodes: Seq[OriginalWorkflowNode]
) derives Encoder.AsObject,
      Decoder
