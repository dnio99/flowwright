package com.dnio.jmespath.function

import com.dnio.jmespath.JmespathZio
import io.circe.Json
import zio.Scope
import zio.ZIO
import zio.ZLayer
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes

object JsonFunctionSpec extends ZIOSpecDefault {

  val layer = ZLayer.make[JmespathZio.Service](
    JmespathZio.live
  )
  override def spec: Spec[TestEnvironment & Scope, Any] = suite(
    "JsonFunctionSpec"
  )(
    test("StringToJsonFunction") {
      (for {
        jmespathSvc <- ZIO.service[JmespathZio.Service]

        jsonStr =
          "{\"browsers\":{\"firefox\":{\"name\":\"Firefox\",\"pref_url\":\"about:config\",\"releases\":{\"1\":{\"release_date\":\"2004-11-09\",\"status\":\"retired\",\"engine\":\"Gecko\",\"engine_version\":\"1.7\"}}}}}"

        json = Json.fromString(jsonStr)
        _ <- jmespathSvc.searchT[Json](
          json,
          "string_to_json(@)"
        )
      } yield (assertCompletes)).provideLayer(layer)
    }
  )
}
