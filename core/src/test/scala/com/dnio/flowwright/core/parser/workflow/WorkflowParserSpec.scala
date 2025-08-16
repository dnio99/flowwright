package com.dnio.flowwright.core.parser.workflow

import com.dnio.flowwright.core.parser.WorkflowParser
import zio.Scope
import zio.ZIO
import zio.test.{
  Spec,
  TestEnvironment,
  ZIOSpecDefault,
  assertCompletes,
  assertTrue
}

object WorkflowParserSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("Workflow Parser Spec")(
      test("Workflow Parse Start End Test Success") {
        val str = """{
                    |  "id": "workflow test",
                    |  "name": "工作流测试",
                    |  "nodes": [
                    |    {
                    |      "id": "start_node",
                    |      "name": "Start Node",
                    |      "description": "开始节点测试",
                    |      "body": {
                    |        "input": {
                    |          "tenantId": "__{tenantId}__"
                    |        }
                    |      },
                    |      "kind": "Start",
                    |      "inputValidator": {
                    |        "type": "object",
                    |        "properties": {
                    |          "tenantId": {
                    |            "type": "string",
                    |            "minLength": 1
                    |          }
                    |        },
                    |        "required": ["tenantId"]
                    |      },
                    |      "outputValidator": {
                    |        "type": "object",
                    |        "properties": {
                    |          "status": {
                    |            "type": "string",
                    |            "enum": ["success", "error"]
                    |          },
                    |          "data": {
                    |            "type": "object"
                    |          }
                    |        },
                    |        "required": ["status"]
                    |      }
                    |    },
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
                    |      "dependentOn": ["start_node"],
                    |      "kind": "HttpRequest",
                    |      "inputValidator": {
                    |        "type": ["object", "null"]
                    |      }
                    |    },
                    |    {
                    |      "id": "end",
                    |      "name": "结束",
                    |      "dependentOn": ["http request"],
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
          _ <- ZIO.fromEither(WorkflowParser.parse(str))
        } yield assertCompletes
      },
      test("Workflow Parse Start End Test Failure") {
        val str = """{
                 |  "id": "workflow test",
                 |  "name": "工作流测试",
                 |  "nodes": [
                 |    {
                 |      "id": "start_node",
                 |      "name": "Start Node",
                 |      "description": "开始节点测试",
                 |      "body": {
                 |        "input": {
                 |          "tenantId": "__{tenantId}__"
                 |        }
                 |      },
                 |      "kind": "Start",
                 |      "inputValidator": {
                 |        "type": "object",
                 |        "properties": {
                 |          "tenantId": {
                 |            "type": "string",
                 |            "minLength": 1
                 |          }
                 |        },
                 |        "required": ["tenantId"]
                 |      },
                 |      "outputValidator": {
                 |        "type": "object",
                 |        "properties": {
                 |          "status": {
                 |            "type": "string",
                 |            "enum": ["success", "error"]
                 |          },
                 |          "data": {
                 |            "type": "object"
                 |          }
                 |        },
                 |        "required": ["status"]
                 |      }
                 |    },
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
          failure <- ZIO.fromEither(WorkflowParser.parse(str)).isFailure
        } yield assertTrue(failure)
      }
    )
}
