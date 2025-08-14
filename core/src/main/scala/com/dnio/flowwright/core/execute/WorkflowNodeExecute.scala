package com.dnio.flowwright.core.execute

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowNodeAbnormalError
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowNodeNotFoundError
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.task.TaskStatus
import com.dnio.flowwright.core.task.WorkflowTask
import com.dnio.flowwright.core.task.WorkflowTaskState
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import org.http4s.client.Client
import zio.Task
import zio.ZIO

import java.time.Instant

object WorkflowNodeExecute {

  private def resolveTaskState(
      node: WorkflowNode,
      workflowState: WorkflowTaskState
  ): ZIO[Any, WorkflowErrors.WorkflowError, WorkflowTask] = {
    val nodeId = node.id
    workflowState.modify(stateMap => {
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

      val dependentNodeFailed = dependenciesStatus.exists(_.isFailed)

      val dependentNodeRunning = dependenciesStatus.exists {
        case TaskStatus.Running() => true
        case TaskStatus.Pending() => true
        case _                    => false
      }

      val dependentNodeSuccess =
        dependenciesStatus.count(_.isSuccess) == dependenciesStatus.size

      (
        dependentNodeFailed,
        dependentNodeRunning,
        dependentNodeSuccess,
        currentTask.status
      ) match {
        case (true, _, _, _) =>
          val task = currentTask.copy(
            status = TaskStatus.Failed(
              WorkflowNodeAbnormalError(description =
                Some("Dependent node is in failed state")
              )
            ),
            updatedAt = Instant.now()
          )
          (task, stateMap.updated(nodeId, task))
        case (_, true, _, _) =>
          val task = currentTask.copy(
            status = TaskStatus.Pending(),
            updatedAt = Instant.now()
          )
          (task, stateMap.updated(nodeId, task))
        case (_, _, true, TaskStatus.Pending()) =>
          val task = currentTask.copy(
            status = TaskStatus.Waiting(),
            updatedAt = Instant.now()
          )
          (task, stateMap.updated(nodeId, task))

        case _ =>
          (currentTask, stateMap.updated(nodeId, currentTask))
      }
    })

  }

  private def execute(
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState,
      workflowNode: WorkflowNode
  ): ZIO[Client[
    Task
  ] & JmespathZio.Service, WorkflowErrors.WorkflowError, Json] = {
    workflowNode match {
      case endNode: WorkflowNode.EndNode =>
        endNode.execute(data, workflowTaskState)
      case httpRequestNode: WorkflowNode.HttpRequestNode =>
        httpRequestNode.execute(data, workflowTaskState)
    }
  }

  def run(
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState,
      workflowNode: WorkflowNode
  ): ZIO[
    Client[Task] & JmespathZio.Service,
    WorkflowErrors.WorkflowError,
    Unit
  ] = {

    val nodeId = workflowNode.id
    for {
      task <- resolveTaskState(
        node = workflowNode,
        workflowState = workflowTaskState
      )

      _ <- task.status match {
        case TaskStatus.Waiting() =>
          workflowTaskState.update(
            _.updated(
              nodeId,
              task.copy(
                status = TaskStatus.Running(),
                updatedAt = Instant.now()
              )
            )
          ) *> execute(
            data = data,
            workflowTaskState = workflowTaskState,
            workflowNode = workflowNode
          )
        case TaskStatus.Failed(workFlowError) =>
          ZIO.logWarning(
            s"node: ${workflowNode.name} run failure! msg:${workFlowError.toString}"
          ) *> ZIO.fail(workFlowError)
        case _ =>
          ZIO.logInfo(
            s"node: ${workflowNode.name} is in an unexpected state: ${task.status}"
          )
      }
    } yield ()

  }

}
