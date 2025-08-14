package com.dnio.flowwright.core.parser

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.node.NodeId
import io.circe.parser.*
import zio.Scope
import zio.ZIO
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes
import zio.test.assertTrue

object WorkflowParserSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("WorkflowParserSpec")(
      test("Parse Workflow Test") {
        val jsonStr = """{
                        |  "id": "workflow test",
                        |  "name": "工作流测试",
                        |  "nodes": [
                        |    {
                        |      "id": "http request",
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
                        |        "type": [
                        |          "object",
                        |          "null"
                        |        ]
                        |      }
                        |    },
                        |    {
                        |      "id": "end",
                        |      "name": "结束",
                        |      "description": "结束",
                        |      "body": {
                        |        "output": {}
                        |      },
                        |      "kind": "End"
                        |    }
                        |  ]
                        |}""".stripMargin

        for {
          json <- ZIO
            .fromEither(parse(jsonStr))
            .mapError(e =>
              WorkflowErrors.WorkflowParseError(
                "Failed to parse workflow JSON: " + e.getMessage
              )
            )
          _ <- ZIO.fromEither(WorkflowParser.parse(json))
        } yield assertCompletes
      },
      test("Parse Workflow Fail Test") {

        val jsonStr = """{
                        |  "id": "workflow test",
                        |  "name": "工作流测试",
                        |  "nodes": [
                        |    {
                        |      "id": "http request",
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
                        |      "id": "http request",
                        |      "name": "请求测试",
                        |      "description": "测试HTTP请求",
                        |      "dependentOn": ["error"],
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
                        |    }
                        |  ]
                        |}
                        |""".stripMargin

        for {
          json <- ZIO
            .fromEither(parse(jsonStr))
            .mapError(e =>
              WorkflowErrors.WorkflowParseError(
                "Failed to parse workflow JSON: " + e.getMessage
              )
            )
          failure <- ZIO.fromEither(WorkflowParser.parse(json)).isFailure
        } yield assertTrue(failure)
      },
      test("Parse Workflow Cycle Test") {

        val jsonStr = """{
                        |  "id": "workflow test",
                        |  "name": "工作流测试",
                        |  "nodes": [
                        |    {
                        |      "id": "http request",
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
                        |        "type": [
                        |          "object",
                        |          "null"
                        |        ]
                        |      }
                        |    },
                        |    {
                        |      "id": "http request",
                        |      "name": "请求测试",
                        |      "description": "测试HTTP请求",
                        |      "dependentOn": [
                        |        "http request"
                        |      ],
                        |      "body": {
                        |        "url": "https://httpbin.nadileaf.com/get",
                        |        "headers": {
                        |          "X-Tenant-Id": "__{tenantId}__"
                        |        }
                        |      },
                        |      "kind": "HttpRequest",
                        |      "inputValidator": {
                        |        "type": [
                        |          "object",
                        |          "null"
                        |        ]
                        |      }
                        |    }
                        |  ]
                        |}""".stripMargin

        for {
          json <- ZIO
            .fromEither(parse(jsonStr))
            .mapError(e =>
              WorkflowErrors.WorkflowParseError(
                "Failed to parse workflow JSON: " + e.getMessage
              )
            )
          failure <- ZIO.fromEither(WorkflowParser.parse(json)).isFailure

        } yield assertTrue(failure)
      },
      test("Parse Workflow Result") {

        val jsonStr = """{
                        |  "id": "workflow test",
                        |  "name": "工作流测试",
                        |  "nodes": [
                        |    {
                        |      "id": "http request",
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
                        |      "id": "http request 1",
                        |      "name": "请求测试1",
                        |      "description": "测试HTTP请求1",
                        |      "body": {
                        |        "url": "https://httpbin.nadileaf.com/get",
                        |        "headers": {
                        |          "X-Tenant-Id": "__{tenantId}__"
                        |        }
                        |      },
                        |      "dependentOn": ["http request"],
                        |      "kind": "HttpRequest",
                        |      "inputValidator": {
                        |        "type": ["object", "null"]
                        |      }
                        |    },
                        |    {
                        |      "id": "end",
                        |      "name": "结束",
                        |      "description": "结束",
                        |      "body": {
                        |        "output": {}
                        |      },
                        |      "kind": "End"
                        |    }
                        |  ]
                        |}
                        |""".stripMargin

        for {
          json <- ZIO
            .fromEither(parse(jsonStr))
            .mapError(e =>
              WorkflowErrors.WorkflowParseError(
                "Failed to parse workflow JSON: " + e.getMessage
              )
            )
          workflow <- ZIO.fromEither(WorkflowParser.parse(json))

          bool = workflow.leafNodeIds.contains(
            NodeId("http request 1")
          ) && workflow.childrenNodes.contains(NodeId("http request"))

        } yield assertTrue(bool)
      }
    )
}
