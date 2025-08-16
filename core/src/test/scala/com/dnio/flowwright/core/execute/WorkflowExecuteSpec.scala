package com.dnio.flowwright.core.execute

import com.dnio.flowwright.core.parser.WorkflowParser
import com.dnio.flowwright.core.test_layer.Http4sLayer
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import org.http4s.client.Client
import zio.Scope
import zio.Task
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes

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
      },
      test("Execute Workflow Test 01") {

        val jsonStr = """{
                        |    "id": "workflow test",
                        |    "name": "工作流测试",
                        |    "nodes": [
                        |        {
                        |            "id": "get_job_entity_by_id",
                        |            "name": "获取职位实体",
                        |            "description": "获取职位实体",
                        |            "body": {
                        |                "url": "https://ruleengine.nadileaf.com/v2/entity/__{tenantId}__/Job/__{jobId}__"
                        |            },
                        |            "kind": "HttpRequest"
                        |        },
                        |        {
                        |            "id": "get_recommend_config",
                        |            "name": "获取推荐配置",
                        |            "description": "获取推荐配置",
                        |            "body": {
                        |                "url": "https://ruleengine.nadileaf.com/v2/entity/standard/GlobalConfig/__{recommendConfig}__",
                        |                "postProcessExpression": "string_to_json(data.standardFields.config)"
                        |            },
                        |            "kind": "HttpRequest"
                        |        },
                        |         {
                        |            "id": "recommend",
                        |            "name": "以岗推人",
                        |            "description": "以岗推人",
                        |             "dependentOn": [
                        |                "get_job_entity_by_id",
                        |                "get_recommend_config"
                        |            ],
                        |            "body": {
                        |                "method": "POST",
                        |                "url": "https://effex-recsys.nadileaf.com/v2/search",
                        |                "body": {
                        |                    "config": "__{get_recommend_config}__",
                        |                    "data": "__{get_job_entity_by_id}__"
                        |                }
                        |            },
                        |            "kind": "HttpRequest"
                        |        },
                        |        {
                        |            "id": "end",
                        |            "name": "结束",
                        |            "dependentOn": [
                        |                "recommend"
                        |            ],
                        |            "description": "结束",
                        |            "body": {
                        |                "output": {
                        |                    "body": "__{get_job_entity_by_id}__",
                        |                    "config": "__{get_recommend_config}__",
                        |                    "result": "__{recommend}__"
                        |                }
                        |            },
                        |            "kind": "End"
                        |        }
                        |    ]
                        |}""".stripMargin
        (for {

          workflow <- ZIO.fromEither(WorkflowParser.parse(jsonStr))

          res <- WorkflowExecute.execute(
            workflow,
            Map(
              "tenantId" -> Json.fromString("yingcaiwanglianbailing"),
              "jobId" -> Json.fromString("5375799"),
              "recommendConfig" -> Json.fromString(
                "yingcaiwanglianbailing-recsys-job_resume-public"
              )
            )
          )

          _ <- ZIO.logInfo(res.noSpaces)

        } yield assertCompletes).provideLayer(layer)
      }
    )
}
