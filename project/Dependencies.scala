import sbt.*

object Dependencies {

  object Versions {
    val tapir = "1.11.41"
    val http4s = "0.23.30"
    val zio = "2.1.20"
    val zioLogging = "2.5.1"
    val zioInteropCats = "23.1.0.5"
    val zioNio = "2.0.2"
    val logback = "1.5.18"
    val circe = "0.14.14"
  }

  val tapir: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio",
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle",
  ).map(_ % Versions.tapir)

  val http4s: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-core",
    "org.http4s" %% "http4s-circe"
  ).map(_ % Versions.http4s)

  val http4sClient: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-ember-client",
  ).map(_ % Versions.http4s)

  val http4sServer: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-ember-server"
  ).map(_ % Versions.http4s)

  val http4sFamily: Seq[ModuleID] = http4s ++ http4sClient ++ http4sServer


  val zioLogging: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-logging",
    "dev.zio" %% "zio-logging-slf4j"
  ).map(_ % Versions.zioLogging)

  val logging: Seq[ModuleID] = Seq(
    "ch.qos.logback" % "logback-classic" % Versions.logback
  ) ++ zioLogging

  val zioCore: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio",
    "dev.zio" %% "zio-streams",
    "dev.zio" %% "zio-test",
  ).map(_ % Versions.zio)

  val zioInterop: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats,
    "dev.zio" %% "zio-nio" % Versions.zioNio
  )

  val circeFamily: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  val coreDependencies: Seq[ModuleID] =
    tapir ++
      http4sFamily ++
      zioLogging ++
      logging ++
      zioCore ++
      zioInterop ++
      circeFamily
}

