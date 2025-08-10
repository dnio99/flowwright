package com.dnio.flowwright.core.template

import com.dnio.flowwright.core.test_layer.JmespathLayer
import com.dnio.jmespath.JmespathZio
import io.circe.Json
import zio.{Ref, Scope, ZIO, ZLayer}
import io.circe.syntax.*
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertCompletes}

object TemplateSpec extends ZIOSpecDefault {

  val layer: ZLayer[Any, Nothing, JmespathZio.Service] = ZLayer.make[JmespathZio.Service](
    JmespathLayer.live
  )

  override def spec: Spec[TestEnvironment & Scope, Any] = suite("TemplateSpec")(
    test("Template Resolver") {
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
            "skills" -> Json.fromString("__{skills[0]}__, __{skills[1]}__"),
            "skillTest" -> Json.fromString("__{skillTest}__")
          )

          res <- TemplateResolver.handle(
            dataRef = ref,
            template = template
          )
          _ <- ZIO.logInfo(res.asJson.noSpaces)
        } yield assertCompletes
      ).provideLayer(layer)

    }
  )
}
