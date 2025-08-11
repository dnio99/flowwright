package com.dnio.flowwright.core.schema

import io.circe.Json
import net.reactivecore.cjs.{DocumentValidator, Loader}
import zio.{Scope, ZIO}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object SchemaSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("SchemaSpec")(
    test("test schema validation Null") {
      val schemaJson = """{
                         |  "type": ["object", "null"],
                         |  "title": "title",
                         |  "additionalProperties": false,
                         |  "properties": {}
                         |}""".stripMargin
      for {
        documentValidator <- ZIO.fromEither(Loader.empty.fromJson(schemaJson))

        result = documentValidator.validate(
          Json.Null
        )
      } yield assertTrue(result.violations.isEmpty)

    },
    test("test schema validation additionalProperties") {
      val schemaJson = """{
                         |  "type": ["object", "null"],
                         |  "title": "title",
                         |  "additionalProperties": false,
                         |  "properties": {}
                         |}""".stripMargin
      for {
        documentValidator <- ZIO.fromEither(Loader.empty.fromJson(schemaJson))

        result = documentValidator.validate(
          Json.obj("test" -> Json.fromString("test"))
        )
      } yield assertTrue(result.violations.nonEmpty)

    }
  )
}
