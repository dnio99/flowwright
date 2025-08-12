package com.dnio.flowwright.core.node

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.interop.jmespath._
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import zio.Ref
import zio.ZIO

trait WorkflowNodeBody { self =>

  val postProcessExpression: Option[String]

  type T

  type R

  def resolver(
      data: Ref[Map[String, Json]]
  ): ZIO[JmespathZio.Service, WorkflowError, T]

  protected def logic(
      data: WorkflowContextData
  ): ZIO[R, WorkflowError, Json]

  def run(
      data: WorkflowContextData
  ): ZIO[R & JmespathZio.Service, WorkflowError, Json] = {
    for {
      originalRes <- logic(data)
      res <- (originalRes, postProcessExpression) match {
        case (json, Some(expression)) if !json.isNull => {
          for {
            jmespath <- ZIO.service[JmespathZio.Service]
            res <- jmespath
              .search(json, expression)
              .mapError(
                _.toWorkflowError
              )
          } yield res
        }
        case _ => ZIO.succeed(originalRes)
      }
    } yield res
  }
}
