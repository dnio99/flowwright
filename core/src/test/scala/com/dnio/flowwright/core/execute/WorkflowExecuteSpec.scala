package com.dnio.flowwright.core.execute

import com.dnio.flowwright.core.parser.WorkflowParser
import com.dnio.flowwright.core.test_layer.Http4sLayer
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import org.http4s.client.Client
import zio.{Scope, Task, ZIO, ZLayer}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}

object WorkflowExecuteSpec extends ZIOSpecDefault {

  val layer = ZLayer.make[Client[Task] & JmespathZio.Service](
    Http4sLayer.live,
    JmespathZio.live
  )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("WorkflowExecuteSpec")(
      test("Execute Workflow Test") {

        val jsonStr = """{
                        |  "id": "workflow test",
                        |  "name": "工作流测试",
                        |  "nodes": [
                        |    {
                        |      "id": "http_request",
                        |      "name": "请求测试",
                        |      "description": "测试HTTP请求",
                        |      "body": {
                        |        "url": "https://httpbin.nadileaf.com/get",
                        |        "headers": {
                        |          "X-Tenant-Id": "__{tenantId}__"
                        |        }
                        |      },
                        |      "kind": "HttpRequest",
                        |      "inputValidator": {
                        |        "type": ["object", "null"]
                        |      }
                        |    },
                        |    {
                        |      "id": "http_request_1",
                        |      "name": "请求测试1",
                        |      "description": "测试HTTP请求1",
                        |      "body": {
                        |        "url": "https://httpbin.nadileaf.com/get",
                        |        "headers": {
                        |          "X-Tenant-Id": "__{tenantId}__"
                        |        }
                        |      },
                        |      "dependentOn": ["http_request"],
                        |      "kind": "HttpRequest",
                        |      "inputValidator": {
                        |        "type": ["object", "null"]
                        |      }
                        |    },
                        |    {
                        |      "id": "end",
                        |      "name": "结束",
                        |      "dependentOn": ["http_request_1"],
                        |      "description": "结束",
                        |      "body": {
                        |        "output": {
                        |            "headers": "__{http_request_1.headers}__"
                        |        }
                        |      },
                        |      "kind": "End"
                        |    }
                        |  ]
                        |}
                        |""".stripMargin
        (for {

          workflow <- ZIO.fromEither(WorkflowParser.parse(jsonStr))

          res <- WorkflowExecute.execute(
            workflow,
            Map("tenantId" -> Json.fromString("test-tenant"))
          )

          _ <- ZIO.logInfo(res.noSpaces)

        } yield assertCompletes).provideLayer(layer)
      }
    )
}
