package com.dnio.flowwright.core.workflow

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowNodeAbnormalError
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowNodeNotFoundError
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.task.TaskStatus
import com.dnio.flowwright.core.task.WorkflowTask
import io.circe.Json
import zio.Ref
import zio.ZIO

import scala.annotation.unused

object WorkflowExecute {

  @unused
  private def tryRunTask(
      node: WorkflowNode,
      workflowState: Ref[Map[NodeId, WorkflowTask]]
  ): ZIO[Any, WorkflowErrors.WorkflowError, NodeId] = {
    val nodeId = node.id
    for {
      task <- workflowState.modify(stateMap => {
        val currentTask = stateMap.getOrElse(
          nodeId,
          WorkflowTask(
            id = nodeId,
            status = TaskStatus.Failed(
              WorkflowNodeNotFoundError(description =
                Some(s"Node not found: ${nodeId}")
              )
            ),
            result = None
          )
        )

        val dependenciesStatus = node.dependentOn.map(depId =>
          stateMap
            .get(depId)
            .map(_.status)
            .getOrElse(
              TaskStatus.Failed(
                WorkflowNodeNotFoundError(description =
                  Some(s"Dependency node not found: ${depId}")
                )
              )
            )
        )

        (
          dependenciesStatus
            .count(
              _.isInstanceOf[TaskStatus.Succeeded]
            ) == node.dependentOn.size,
          currentTask.status
        ) match {
          case (true, TaskStatus.Pending()) =>
            val task = currentTask.copy(
              status = TaskStatus.Running()
            )
            (
              task,
              stateMap.updated(nodeId, task)
            )
          case (false, _) =>
            val task = currentTask.copy(
              status = TaskStatus.Failed(
                WorkflowNodeAbnormalError(description =
                  Some(
                    s"The dependent node of node ${nodeId} is in abnormal state"
                  )
                )
              )
            )
            (task, stateMap.updated(nodeId, task))
          case _ =>
            val task = currentTask.copy(
              status = TaskStatus.Failed(
                WorkflowNodeAbnormalError(description =
                  Some(s"Node ${nodeId} is already running or succeeded")
                )
              )
            )
            (task, stateMap.updated(nodeId, task))
        }

      })

      res <- task match {
        case WorkflowTask(nodeId, TaskStatus.Failed(workFlowError), _) =>
          ZIO.logWarning(
            s"node task: ${nodeId} failed: ${workFlowError.toString}"
          ) *> ZIO.fail(workFlowError)
        case _ =>
          ZIO.succeed(task.id)
      }

    } yield res

  }

  def execute(workflow: Workflow) = {
    val initTasks =
      workflow.nodes.keys.map(nodeId => (nodeId, WorkflowTask(nodeId))).toMap

    for {
      workflowState <- Ref.make(initTasks)

      workflowContextData <- Ref.make(Map.empty[String, Json])
    } yield ()
  }

}
