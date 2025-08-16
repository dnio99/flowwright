package com.dnio.flowwright.core.parser.node

import com.dnio.flowwright.core.parser.NodeParser
import zio.Scope
import zio.ZIO
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes
import zio.test.assertTrue

object StartNodeParserSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("StartNodeParserSpec")(
      test("StartNode Parse Test Failure") {
        val jsonStr = """{
                      |  "id": "start_node",
                      |  "name": "Start Node",
                      |  "description": "开始节点测试",
                      |  "body": {
                      |    "input": {
                      |      "tenantId": "__{tenantId}__"
                      |    }
                      |  },
                      |  "kind": "Start"
                      |}
                      |""".stripMargin

        for {
          failure <- ZIO.fromEither(NodeParser.parse(jsonStr)).isFailure
        } yield assertTrue(failure)
      },
      test("StartNode Parse Test Success") {
        val jsonStr =
          """
            |{
            |  "id": "start_node",
            |  "name": "Start Node",
            |  "description": "开始节点测试",
            |  "body": {
            |    "input": {
            |      "tenantId": "__{tenantId}__"
            |    }
            |  },
            |  "kind": "Start",
            |  "inputValidator": {
            |    "type": "object",
            |    "properties": {
            |      "tenantId": {
            |        "type": "string",
            |        "minLength": 1
            |      }
            |    },
            |    "required": ["tenantId"]
            |  },
            |  "outputValidator": {
            |    "type": "object",
            |    "properties": {
            |      "status": {
            |        "type": "string",
            |        "enum": ["success", "error"]
            |      },
            |      "data": {
            |        "type": "object"
            |      }
            |    },
            |    "required": ["status"]
            |  }
            |}
            |""".stripMargin

        for {
          _ <- ZIO.fromEither(NodeParser.parse(jsonStr))
        } yield assertCompletes
      }
    )
}
