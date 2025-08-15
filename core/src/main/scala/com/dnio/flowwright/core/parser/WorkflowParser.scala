package com.dnio.flowwright.core.parser

import cats.implicits._
import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.NodeKind
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.node.WorkflowNode._
import com.dnio.flowwright.core.workflow.OriginalWorkflow
import com.dnio.flowwright.core.workflow.Workflow
import com.dnio.flowwright.core.workflow.WorkflowId
import io.circe.Json
import io.circe.parser._

object WorkflowParser {

  private def checkDependentOn(
      workflowNodes: Seq[WorkflowNode]
  ): Either[WorkflowErrors.WorkflowNodeNotFoundError, Seq[WorkflowNode]] = {
    val map = workflowNodes.map(node => (node.id -> node)).toMap

    workflowNodes
      .flatMap(
        _.dependentOn
      )
      .map(nodeId =>
        map
          .get(nodeId)
          .toRight(
            WorkflowErrors.WorkflowNodeNotFoundError(
              s"node with id $nodeId not found in workflow"
            )
          )
      )
      .sequence
  }

  private def hasCycle(workflowNodes: Seq[WorkflowNode]) = {

    val nodeMap = workflowNodes.map(node => (node.id, node)).toMap

    def loop(
        nodeId: NodeId,
        visiting: Set[NodeId],
        visited: Set[NodeId]
    ): (Boolean, Set[NodeId]) = {
      if (visited.contains(nodeId)) {
        (false, visited) // already visited, no cycle
      } else if (visiting.contains(nodeId)) {
        (true, visited) // found a cycle
      } else {
        val newVisiting = visiting + nodeId

        val dependentIds =
          nodeMap.get(nodeId).map(_.dependentOn).getOrElse(Seq.empty)

        val (cycleFound, updatedVisited) =
          dependentIds.foldLeft((false, visited)) {
            case ((found, currentVisited), dependentId) =>
              // 如果已经找到环，则不再继续递归
              if (found) {
                (true, currentVisited)
              } else {
                // 对依赖节点进行递归检查
                val (cycle, newVisited) =
                  loop(dependentId, newVisiting, currentVisited)
                (cycle, newVisited)
              }
          }
        val finalVisited =
          if (!cycleFound) updatedVisited + nodeId else updatedVisited
        (cycleFound, finalVisited)
      }
    }

    workflowNodes
      .foldLeft((false, Set.empty[NodeId])) {
        case ((cycleFound, visited), node) =>
          if (cycleFound) {
            (true, visited)
          } else {
            val (cycle, newVisited) = loop(node.id, Set.empty[NodeId], visited)
            (cycle, newVisited)
          }
      }
      ._1

  }

  private def verifyEndNode(
      workflowNodes: Seq[WorkflowNode],
      leafNodeIds: Seq[NodeId]
  ): Either[WorkflowErrors.WorkflowNodeValidationError, EndNode] = {

    val endNodes = workflowNodes.filter(_.kind == NodeKind.End)

    val parentIds = workflowNodes.flatMap(_.dependentOn).toSet

    endNodes match {
      case endNode :: Nil =>
        (parentIds.contains(endNode.id), endNode) match {
          case (false, endNode: EndNode) =>
            Right(
              endNode.copy(
                dependentOn = leafNodeIds
              )
            )
          case _ =>
            Left(
              WorkflowErrors.WorkflowNodeValidationError(
                s"The end node must not have any dependencies. ${endNode.id}"
              )
            )
        }
      case _ =>
        Left(
          WorkflowErrors.WorkflowNodeValidationError(
            "Workflow must have exactly one end node"
          )
        )
    }

  }

  private def getLeafNodes(
      workflowNodes: Seq[WorkflowNode]
  ): Seq[WorkflowNode] = {

    val dependentOnIds = workflowNodes.flatMap(_.dependentOn).toSet

    workflowNodes.filter(node => !dependentOnIds.contains(node.id))
  }

  private def checkNodeId(
      workflowNodes: Seq[WorkflowNode]
  ): Either[WorkflowErrors.WorkflowNodeValidationError, Unit] = if (
    workflowNodes.map(_.id).toSet.size == workflowNodes.size
  ) Right(())
  else
    Left(
      WorkflowErrors.WorkflowNodeValidationError(
        "Workflow nodes must have unique IDs"
      )
    )

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

      _ <- checkNodeId(workflowNodes)

      // check dependentOn
      _ <- checkDependentOn(workflowNodes)

      _ <-
        if (hasCycle(workflowNodes)) {
          Left(
            WorkflowErrors.WorkflowCycleError(
              s"Workflow with id ${workflowId.asString} has cycle dependencies"
            )
          )
        } else {
          Right(())
        }

      nodes = workflowNodes.filterNot(_.kind == NodeKind.End)

      leafNodeIds = getLeafNodes(nodes).map(_.id)

      endNode <- verifyEndNode(workflowNodes, leafNodeIds)

      childrenNodes = nodes
        .flatMap(node => node.dependentOn.map(_ -> node))
        .groupMap(
          _._1
        )(
          _._2
        )
    } yield Workflow(
      id = workflowId,
      name = originalWorkflow.name,
      description = originalWorkflow.description,
      // remove end node from nodes
      nodes = nodes,
      endNode = endNode,
      leafNodeIds = leafNodeIds.toSet,
      childrenNodes = childrenNodes
    )

  }

  def parse(string: String): Either[WorkflowError, Workflow] =
    decode[OriginalWorkflow](string).left
      .map(e =>
        WorkflowErrors.WorkflowParseError(
          s"Failed to parse workflow JSON: ${e.getMessage}",
          Some(e.getMessage)
        )
      )
      .flatMap(
        parse
      )

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
