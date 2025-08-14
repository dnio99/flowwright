package com.dnio.flowwright.core.parser

import cats.implicits._
import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.OriginalWorkflowNode
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.node.WorkflowNode._
import com.dnio.flowwright.core.node.body.WorkflowNodeBody._
import com.dnio.flowwright.core.parser.body.BodyParser
import io.circe.Json
import net.reactivecore.cjs.Loader

object NodeParser {

  def parse(
      json: Json
  ): Either[WorkflowError, WorkflowNode] = {
    json
      .as[OriginalWorkflowNode]
      .left
      .map(e =>
        WorkflowErrors
          .WorkflowNodeParseError("Failed to parse node", Some(e.getMessage))
      )
      .flatMap(
        parse
      )
  }

  def parse(
      originalWorkflowNode: OriginalWorkflowNode
  ): Either[WorkflowError, WorkflowNode] = {
    for {
      inputValidator <- originalWorkflowNode.inputValidator
        .traverse { json =>
          Loader.empty.fromJson(json)
        }
        .left
        .map(e =>
          WorkflowErrors
            .WorkflowNodeParseError(
              "Failed to parse input validator",
              Some(e.message)
            )
        )

      outputValidator <- originalWorkflowNode.outputValidator
        .traverse { json =>
          Loader.empty.fromJson(json)
        }
        .left
        .map(e =>
          WorkflowErrors
            .WorkflowNodeParseError(
              "Failed to parse output validator",
              Some(e.message)
            )
        )

      id = NodeId(originalWorkflowNode.id)

      dependentOn = originalWorkflowNode.dependentOn
        .getOrElse(Seq.empty)
        .map(NodeId(_))

      body <- BodyParser.parse(
        originalWorkflowNode.kind,
        originalWorkflowNode.body
      )

      res = body match {
        case httpRequestBody: HttpRequestBody =>
          HttpRequestNode(
            id = id,
            name = originalWorkflowNode.name,
            description = originalWorkflowNode.description,
            dependentOn = dependentOn,
            body = httpRequestBody,
            inputValidator = inputValidator,
            outputValidator = outputValidator
          )
        case endBody: EndBody =>
          EndNode(
            id = id,
            name = originalWorkflowNode.name,
            description = originalWorkflowNode.description,
            dependentOn = dependentOn,
            body = endBody
          )
      }

    } yield res
  }

}
