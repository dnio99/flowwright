package com.dnio.flowwright.core.node
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.body.WorkflowNodeBody
import com.dnio.flowwright.core.node.body.WorkflowNodeBody._
import com.dnio.flowwright.core.node.validator.InputMandatoryValidator
import com.dnio.flowwright.core.node.validator.InputValidator
import com.dnio.flowwright.core.node.validator.OutputMandatoryValidator
import com.dnio.flowwright.core.node.validator.OutputValidator
import com.dnio.flowwright.core.task.TaskStatus
import com.dnio.flowwright.core.task.WorkflowTask
import com.dnio.flowwright.core.task.WorkflowTaskState
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import net.reactivecore.cjs.DocumentValidator
import org.http4s.client.Client
import zio.Task
import zio.ZIO

sealed trait WorkflowNode {

  type R

  val id: NodeId
  val name: String
  val description: Option[String]
  val dependentOn: Seq[NodeId]
  val body: WorkflowNodeBody[R, ?]

  val kind: NodeKind

  def execute(
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState
  ): ZIO[R & JmespathZio.Service, WorkflowError, Json] = {
    val logic = for {
      res <- body.run(data)

      _ <- data.update(map =>
        map.updated(
          id.asString,
          res
        )
      )
    } yield res

    logic.tapBoth(
      e =>
        workflowTaskState.update(
          _.updated(
            id,
            WorkflowTask(id = id, status = TaskStatus.Failed(e), result = None)
          )
        ),
      json =>
        workflowTaskState
          .update(
            _.updated(
              id,
              WorkflowTask(
                id = id,
                status = TaskStatus.Succeeded(),
                result = None
              )
            )
          )
          .as(json)
    )

  }

}

object WorkflowNode {

  final case class StartNode(
      id: NodeId,
      name: String,
      description: Option[String] = None,
      dependentOn: Seq[NodeId] = Seq.empty,
      body: StartBody,
      inputValidator: Some[DocumentValidator],
      outputValidator: Some[DocumentValidator]
  ) extends WorkflowNode,
        InputMandatoryValidator,
        OutputMandatoryValidator {

    override val kind: NodeKind = NodeKind.Start

    override type R = JmespathZio.Service
  }

  final case class EndNode(
      id: NodeId,
      name: String,
      description: Option[String] = None,
      dependentOn: Seq[NodeId],
      body: EndBody
  ) extends WorkflowNode {

    override val kind: NodeKind = NodeKind.End

    override type R = JmespathZio.Service

  }

  final case class HttpRequestNode(
      id: NodeId,
      name: String,
      description: Option[String],
      dependentOn: Seq[NodeId],
      body: HttpRequestBody,
      inputValidator: Option[DocumentValidator] = None,
      outputValidator: Option[DocumentValidator] = None
  ) extends WorkflowNode,
        InputValidator,
        OutputValidator {

    override val kind: NodeKind = NodeKind.HttpRequest

    type R = Client[Task] & JmespathZio.Service

  }
}
