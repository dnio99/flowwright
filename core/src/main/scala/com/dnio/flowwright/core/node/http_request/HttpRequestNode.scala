package com.dnio.flowwright.core.node.http_request
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.NodeValidator
import com.dnio.flowwright.core.node.WorkflowNode
import com.dnio.flowwright.core.task.WorkflowTaskState
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import net.reactivecore.cjs.DocumentValidator
import org.http4s.client.Client
import zio.Task
import zio.ZIO

final case class HttpRequestNode(
    id: NodeId,
    name: String,
    description: Option[String],
    dependentOn: Seq[NodeId],
    body: HttpRequestBody,
    inputValidator: Option[DocumentValidator] = None,
    outputValidator: Option[DocumentValidator] = None
) extends WorkflowNode,
      NodeValidator {

  type R = Client[Task] & JmespathZio.Service

  override def execute(
      data: WorkflowContextData,
      workflowTaskState: WorkflowTaskState
  ): ZIO[R, WorkflowError, Unit] = {
    for {
      res <- body.run(data)

      _ <- data.update(map =>
        map.updated(
          id.asString,
          res
        )
      )
    } yield ()
  }

}
