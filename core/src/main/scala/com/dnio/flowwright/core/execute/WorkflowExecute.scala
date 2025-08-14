package com.dnio.flowwright.core.execute
import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.task.WorkflowTask
import com.dnio.flowwright.core.task.WorkflowTaskState
import com.dnio.flowwright.core.workflow.Workflow
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import org.http4s.client.Client
import zio.Ref
import zio.Task
import zio.ZIO

object WorkflowExecute {

  private def checkTermination(
      leafNodeIds: Set[NodeId],
      workflowTaskState: WorkflowTaskState,
      runNodeIds: Set[NodeId]
  ): ZIO[Any, Nothing, Boolean] = {
    val runLeafNodeIds = leafNodeIds.intersect(runNodeIds)

    if (runLeafNodeIds.nonEmpty) {
      // check if all leaf nodes are completed
      for {
        taskStateMap <- workflowTaskState.get

        allCompleted = runLeafNodeIds.forall { nodeId =>
          taskStateMap.get(nodeId).exists(task => task.status.isSuccess)
        }
      } yield allCompleted

    } else {
      ZIO.succeed(false)
    }

  }

  private def terminalLogic(
      workflow: Workflow,
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState
  ): ZIO[JmespathZio.Service, WorkflowErrors.WorkflowError, Json] = {
    val endNode = workflow.endNode
    for {
      res <- endNode.execute(data, workflowTaskState)
    } yield res
  }

  private def runNodes(
      workflow: Workflow,
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState,
      workflowNodes: Seq[WorkflowNode]
  ): ZIO[Client[
    Task
  ] & JmespathZio.Service, WorkflowErrors.WorkflowError, Json] = {

    for {
      // check return
      _ <- ZIO.foreachPar(workflowNodes)(node =>
        WorkflowNodeExecute.run(
          data = data,
          workflowTaskState = workflowTaskState,
          workflowNode = node
        )
      )
      isTerminated <- checkTermination(
        workflow.leafNodeIds,
        workflowTaskState,
        workflowNodes.map(_.id).toSet
      )

      res <-
        if (isTerminated) {
          terminalLogic(workflow, data, workflowTaskState)
        } else {
          val nextNodes = workflowNodes.flatMap(node =>
            workflow.childrenNodes.getOrElse(node.id, Seq.empty)
          )
          runNodes(workflow, data, workflowTaskState, nextNodes)
        }

    } yield res

  }

  def execute(workflow: Workflow, initData: Map[String, Json] = Map.empty): ZIO[Client[
    Task
  ] & JmespathZio.Service, WorkflowErrors.WorkflowError, Json] = {
    val initTasks =
      workflow.nodes.map(node => (node.id, WorkflowTask(node.id))).toMap

    for {
      workflowState <- Ref.make(initTasks)

      workflowContextData <- Ref.make(initData)

      res <- runNodes(
        workflow = workflow,
        data = workflowContextData,
        workflowTaskState = workflowState,
        workflowNodes = workflow.getRootNodes
      )
    } yield res
  }

}
