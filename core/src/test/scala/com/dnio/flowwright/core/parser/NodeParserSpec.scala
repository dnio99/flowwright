package com.dnio.flowwright.core.parser

import com.dnio.flowwright.core.errors.WorkflowErrors
import io.circe.parser._
import zio.Scope
import zio.ZIO
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes
import zio.test.assertTrue

object NodeParserSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("Node Parser")(
    test("Parser HttpRequest Test") {

      val nodeJsonStr = """{
                          |  "id": "http request",
                          |  "name": "请求测试",
                          |  "description": "测试HTTP请求",
                          |  "body": {
                          |    "url": "https://httpbin.nadileaf.com/get",
                          |    "headers": {
                          |      "X-Tenant-Id": "__{tenantId}__"
                          |    }
                          |  },
                          |  "kind": "HttpRequest"
                          |}""".stripMargin

      for {
        json <- ZIO
          .fromEither(parse(nodeJsonStr))
          .mapError(e =>
            WorkflowErrors.WorkflowNodeParseError(
              s"Failed to parse node JSON: ${e.getMessage}"
            )
          )
        _ <- ZIO.fromEither(NodeParser.parse(json))

      } yield assertCompletes

    },
    test("Parser Json Schema Fail Test") {

      val nodeJsonStr = """{
          "id": "http request",
          "name": "请求测试",
          "description": "测试HTTP请求",
          "body": {
            "url": "https://httpbin.nadileaf.com/get",
            "headers": {
              "X-Tenant-Id": "__{tenantId}__"
            }
          },
          "kind": "HttpRequest",
          "inputValidator": {
            "type": [
              "错误的",
              "null"
            ]
          }
        }"""

      for {
        json <- ZIO
          .fromEither(parse(nodeJsonStr))
          .mapError(e =>
            WorkflowErrors.WorkflowNodeParseError(
              s"Failed to parse node JSON: ${e.getMessage}"
            )
          )
        failed <- ZIO
          .fromEither(NodeParser.parse(json))
          .tapError(e => ZIO.logWarning(e.toString))
          .isFailure
      } yield assertTrue(failed)
    },
    test("Parser Json Schema Success Test") {

      val nodeJsonStr = """{
                          |  "id": "http request",
                          |  "name": "请求测试",
                          |  "description": "测试HTTP请求",
                          |  "body": {
                          |    "url": "https://httpbin.nadileaf.com/get",
                          |    "headers": {
                          |      "X-Tenant-Id": "__{tenantId}__"
                          |    }
                          |  },
                          |  "kind": "HttpRequest",
                          |  "inputValidator": {
                          |    "type": ["object", "null"],
                          |    "properties": {
                          |      "url": { "type": "string", "format": "uri" },
                          |      "headers": {
                          |        "type": "object",
                          |        "additionalProperties": { "type": "string" }
                          |      }
                          |    },
                          |    "required": ["url"]
                          |  }
                          |}""".stripMargin

      for {
        json <- ZIO
          .fromEither(parse(nodeJsonStr))
          .mapError(e =>
            WorkflowErrors.WorkflowNodeParseError(
              s"Failed to parse node JSON: ${e.getMessage}"
            )
          )
        _ <- ZIO
          .fromEither(NodeParser.parse(json))

      } yield assertCompletes
    }
  )
}
