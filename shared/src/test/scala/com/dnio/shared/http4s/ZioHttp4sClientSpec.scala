package com.dnio.shared.http4s

import com.dnio.shared.http4s.Http4sClient.LogLevel.Info
import com.dnio.shared.http4s.zio_interop.ZioHttp4sClient.given
import com.dnio.shared.http4s.zio_interop.syntax._
import com.dnio.shared.test_layer.Http4sLayer
import com.dnio.shared.test_layer.LoggingLayer
import io.circe.Json
import org.http4s.client.Client
import zio.Scope
import zio.Task
import zio.ZLayer
import zio.test.Spec
import zio.test.TestEnvironment
import zio.test.ZIOSpecDefault
import zio.test.assertCompletes
object ZioHttp4sClientSpec extends ZIOSpecDefault {

  val layer: ZLayer[Any, Throwable, Client[Task]] = ZLayer.make[Client[Task]](
    LoggingLayer.live,
    Http4sLayer.live
  )

  override def spec: Spec[TestEnvironment & Scope, Any] = (
    suite("ZioHttp4sClientSpec")(
      test("request Test") {
        (
          for {
            _ <- "https://httpbin.nadileaf.com/get".get[Json]
          } yield assertCompletes
        ).provideLayer(
          layer
        )
      },
      test("request Test Logger") {
        given Http4sClient.LoggerConfig = Http4sClient.LoggerConfig(
          logRequestBody = true,
          logResponseBody = true,
          logRequestHeaders = true,
          logResponseHeaders = true,
          logLevel = Some(Info)
        )
        (
          for {
            _ <- "https://httpbin.nadileaf.com/get".get[Json]
          } yield assertCompletes
        ).provideLayer(
          layer
        )
      }
    )
  )
}
