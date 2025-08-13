package com.dnio.flowwright.core.node.http_request
import com.dnio.flowwright.core.node.NodeId
import com.dnio.flowwright.core.task.WorkflowTask
import com.dnio.flowwright.core.test_layer.Http4sLayer
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import io.circe.syntax._
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

object HttpRequestSpec extends ZIOSpecDefault {

  val layer = ZLayer.make[Client[Task] & JmespathZio.Service](
    Http4sLayer.live,
    JmespathZio.live
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HttpRequestSpec")(
      test("HttpRequest Execute") {

        val httpRequestNode = HttpRequestNode(
          id = NodeId("http-request-test"),
          name = "HttpRequestTest",
          description = None,
          dependentOn = Seq.empty,
          body = HttpRequestBody.fromOriginal(
            OriginalHttpRequestBody(
              method = Some("GET"),
              url = "https://httpbin.nadileaf.com/get",
              headers = Some(Map("X-Tenant-Id" -> "__{tenantId}__")),
              body = None,
              bodyType = None,
              postProcessExpression = None
            )
          ),
          inputValidator = None,
          outputValidator = None
        )
        (for {

          data <- Ref.make(Map("tenantId" -> Json.fromString("test-tenant")))

          state <- Ref.make(Map.empty[NodeId, WorkflowTask])
          _ <- httpRequestNode.execute(
            data,
            state
          )
          map <- data.get
          _ <- ZIO.logInfo(s"Response: ${map.asJson.noSpaces}")
        } yield assertCompletes).provideLayer(layer)
      }
    )

}
