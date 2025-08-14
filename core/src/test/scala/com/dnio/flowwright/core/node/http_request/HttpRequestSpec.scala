package com.dnio.flowwright.core.node.http_request
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.node.WorkflowNode._
import com.dnio.flowwright.core.node.body.OriginalHttpRequestBody
import com.dnio.flowwright.core.task.WorkflowTask
import com.dnio.flowwright.core.test_layer.Http4sLayer
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import org.http4s.client.Client
import zio.Ref
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes
import zio.test.assertTrue

object HttpRequestSpec extends ZIOSpecDefault {

  val layer = ZLayer.make[Client[Task] & JmespathZio.Service](
    Http4sLayer.live,
    JmespathZio.live
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HttpRequestSpec")(
      test("HttpRequest Execute") {

        (for {

          httpRequestBody <- ZIO.fromEither(
            OriginalHttpRequestBody(
              method = Some("GET"),
              url = "https://httpbin.nadileaf.com/get",
              headers = Some(Map("X-Tenant-Id" -> "__{tenantId}__")),
              body = None,
              bodyType = None,
              postProcessExpression = None
            ).toWorkflowNodeBody
          )

          httpRequestNode = HttpRequestNode(
            id = NodeId("http-request-test"),
            name = "HttpRequestTest",
            description = None,
            dependentOn = Seq.empty,
            body = httpRequestBody,
            inputValidator = None,
            outputValidator = None
          )

          data <- Ref.make(Map("tenantId" -> Json.fromString("test-tenant")))

          state <- Ref.make(Map.empty[NodeId, WorkflowTask])
          _ <- httpRequestNode.execute(
            data,
            state
          )
        } yield assertCompletes).provideLayer(layer)
      },
      test("HttpRequest Execute Fail") {

        (for {

          httpRequestBody <- ZIO.fromEither(
            OriginalHttpRequestBody(
              method = Some("GET"),
              url = "https://httpbin.nadileaf.com/status/__{code}__",
              headers = Some(Map("X-Tenant-Id" -> "__{tenantId}__")),
              body = None,
              bodyType = None,
              postProcessExpression = None
            ).toWorkflowNodeBody
          )

          httpRequestNode = HttpRequestNode(
            id = NodeId("http-request-test"),
            name = "HttpRequestTest",
            description = None,
            dependentOn = Seq.empty,
            body = httpRequestBody,
            inputValidator = None,
            outputValidator = None
          )

          data <- Ref.make(
            Map(
              "tenantId" -> Json.fromString("test-tenant"),
              "code" -> Json.fromInt(500)
            )
          )

          state <- Ref.make(Map.empty[NodeId, WorkflowTask])
          fail <- httpRequestNode
            .execute(
              data,
              state
            )
            .isFailure

        } yield assertTrue(fail)).provideLayer(layer)
      }
    )

}
