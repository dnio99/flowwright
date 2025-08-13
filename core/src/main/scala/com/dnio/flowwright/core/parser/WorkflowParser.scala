package com.dnio.flowwright.core.parser

import cats.implicits._
import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.workflow.OriginalWorkflow
import com.dnio.flowwright.core.workflow.Workflow
import com.dnio.flowwright.core.workflow.WorkflowId
import io.circe.Json

object WorkflowParser {

  def parse(
      originalWorkflow: OriginalWorkflow
  ): Either[WorkflowError, Workflow] = {
    val workflowId = WorkflowId(originalWorkflow.id)

    for {

      workflowNodes <- originalWorkflow.nodes
        .map(
          NodeParser.parse
        )
        .sequence

      // check dependentOn
      map = workflowNodes.map(node => (node.id -> node)).toMap

      _ <- workflowNodes
        .flatMap(
          _.dependentOn
        )
        .map(nodeId =>
          map
            .get(nodeId)
            .toRight(
              WorkflowErrors.WorkflowNodeNotFoundError(
                s"node with id $nodeId not found in workflow ${originalWorkflow.id}"
              )
            )
        )
        .sequence

    } yield Workflow(
      id = workflowId,
      name = originalWorkflow.name,
      description = originalWorkflow.description,
      nodes = map
    )

  }

  def parse(
      json: Json
  ): Either[WorkflowError, Workflow] = {
    json
      .as[OriginalWorkflow]
      .left
      .map(e =>
        WorkflowErrors.WorkflowParseError(
          s"Failed to parse workflow JSON: ${e.getMessage}"
        )
      )
      .flatMap(
        parse
      )
  }

}
