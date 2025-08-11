package com.dnio.flowwright.core.node

import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
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

  def run(
      data: WorkflowContextData
  ): ZIO[R, WorkflowError, Json]
}
