import Dependencies.*

import scala.collection.Seq

val scala3Version = "3.7.2"

val rootName: String = "flowwright"

lazy val root = project
  .in(file("."))
  .settings(
    name := rootName
  )
  .aggregate(core)
  .aggregate(shared)

lazy val core = project
  .in(file("core"))
  .enablePlugins(BuildInfoPlugin, DockerPlugin, JavaAppPackaging)
  .settings(commonSettings("core"))
  .settings(
    libraryDependencies ++= coreDependencies
  )

lazy val shared = project
  .in(file("shared"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings("shared"))
  .settings(
    libraryDependencies ++= sharedDependencies
  )

def commonSettings(module: String) =
  Seq(
    organization := "com.dnio.flowwright",
    name := module,
    scalafixOnCompile := true,
    scalafmtOnCompile := true,
    scalaVersion := scala3Version,
    semanticdbEnabled := true, // enable SemanticDB
    scalacOptions ++= Seq(
      "-Wunused:all"
    ),
    javaOptions ++= Seq(
      "--enable-native-access=ALL-UNNAMED"
    ),
    // Disable doc generate
    Compile / doc / sources := Seq.empty,
    Compile / packageDoc / publishArtifact := false
  )

def buildSettings(module: String) = Seq(
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
  buildInfoPackage := s"com.dnio.flowwright.$module",
  buildInfoOptions += BuildInfoOption.BuildTime
)

def dockerSettings(ports: Seq[Int], moduleName: String) = Seq(
  Docker / maintainer := "Dnio Harrison",
  Docker / packageName := s"${rootName}-${moduleName}",
  dockerBaseImage := "ghcr.io/graalvm/jdk-community:24",
  dockerBuildCommand := {
    if (sys.props("os.arch") != "amd64") {
      // use buildx with platform to build supported amd64 images on other CPU architectures
      // this may require that you have first run 'docker buildx create' to set docker buildx up
      dockerExecCommand.value ++ Seq(
        "buildx",
        "build",
        "--platform=linux/amd64",
        "--load"
      ) ++ dockerBuildOptions.value :+ "."
    } else dockerBuildCommand.value
  }
)
