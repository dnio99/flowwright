package com.dnio.flowwright.core.template

import com.dnio.flowwright.core.test_layer.JmespathLayer
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import io.circe.syntax._
import zio.Ref
import zio.Scope
import zio.ZLayer
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertTrue

object TemplateSpec extends ZIOSpecDefault {

  val layer: ZLayer[Any, Nothing, JmespathZio.Service] =
    ZLayer.make[JmespathZio.Service](
      JmespathLayer.live
    )

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("TemplateSpec")(
    test("Template Resolver Object") {
      (
        for {
          ref <- Ref.make(
            Map(
              "name" -> Json.fromString("John Doe"),
              "age" -> Json.fromInt(30),
              "city" -> Json.fromString("New York"),
              "skills" -> Json.arr(
                Json.fromString("Scala"),
                Json.fromString("ZIO"),
                Json.fromString("Akka")
              ),
              "skillTest" -> Json.obj(
                "skill1" -> Json.fromString("Scala"),
                "skill2" -> Json.fromString("ZIO"),
                "skill3" -> Json.fromString("Akka")
              )
            )
          )
          template = Map(
            "name" -> Json.fromString("__{name}__"),
            "age" -> Json.fromString("__{age}__"),
            "city" -> Json.fromString("__{city}__"),
            "skills" -> Json.fromString("__{skills[0]}__"),
            "skillTest" -> Json.fromString("__{skillTest}__")
          )

          res <- TemplateResolver.handle[Map[String, Json], Map[String, Json]](
            dataRef = ref,
            template = template
          )
          expectResult =
            """{"city":"New York","name":"John Doe","age":30,"skillTest":{"skill1":"Scala","skill2":"ZIO","skill3":"Akka"},"skills":"Scala"}"""
          actual = res.asJson.noSpaces
        } yield assertTrue(actual == expectResult)
      ).provideLayer(layer)

    },
    test("Template Resolver String") {
      (
        for {
          ref <- Ref.make(
            Map(
              "openId" -> Json.fromString("dnio"),
              "name" -> Json.fromString("John Doe")
            )
          )
          template =
            "https://api.dnio.com/user/__{openId}__/profile?name=__{name}__"

          res <- TemplateResolver.handle[String, String](
            dataRef = ref,
            template = template
          )
        } yield assertTrue(
          res == "https://api.dnio.com/user/dnio/profile?name=John Doe"
        )
      ).provideLayer(layer)
    },
    test("Template Resolver String 01") {
      (
        for {
          ref <- Ref.make(
            Map(
              "name" -> Json.fromString("John Doe")
            )
          )
          template =
            "https://api.dnio.com/user/__{openId}__/profile?name=__{name}__"

          res <- TemplateResolver.handle[String, String](
            dataRef = ref,
            template = template
          )
        } yield assertTrue(
          res == "https://api.dnio.com/user/null/profile?name=John Doe"
        )
      ).provideLayer(layer)
    }
  )
}
