package com.dnio.jmespath

import com.dnio.jmespath.errors.JmespathError
import com.dnio.jmespath.functions.json.StringToJsonFunction
import com.fasterxml.jackson.databind.JsonNode
import io.burt.jmespath.Expression
import io.burt.jmespath.RuntimeConfiguration
import io.burt.jmespath.function.FunctionRegistry
import io.burt.jmespath.jackson.JacksonRuntime
import io.circe.Decoder
import io.circe.Json
import io.circe.jackson.circeToJackson
import io.circe.jackson.jacksonToCirce
import zio.IO
import zio.Ref
import zio.ZIO
import zio.ZLayer

object JmespathZio {

  trait Service {

    def search(rowData: Json, expression: String): IO[JmespathError, Json]

    def searchT[T](rowData: Json, expression: String)(implicit
        circeDecoder: Decoder[T]
    ): IO[JmespathError, T]

    def mergeResultsWithExpressions(
        rowData: Json,
        expressions: Seq[String]
    ): IO[JmespathError, Json]
  }

  private class ServiceImpl(
      jmesPathRuntime: JacksonRuntime,
      expressionMapRef: Ref[Map[String, Expression[JsonNode]]]
  ) extends Service {

    private def getExpression(
        expressionStr: String
    ): ZIO[Any, JmespathError.ExpressionParseError, Expression[JsonNode]] = {
      for {
        expressionMap <- expressionMapRef.get

        expression <- expressionMap.get(expressionStr) match {
          case Some(value) => ZIO.succeed(value)
          case None        =>
            ZIO
              .attempt(
                jmesPathRuntime.compile(expressionStr)
              )
              .mapError(e =>
                JmespathError.ExpressionParseError(
                  s"expression (${expressionStr}) parse error!",
                  Some(e.getMessage)
                )
              )
        }
        res <- expressionMapRef.modify(map =>
          map.get(expressionStr) match {
            case Some(value) => (value, map)
            case None => (expression, map + (expressionStr -> expression))
          }
        )

      } yield res
    }

    override def search(
        rowData: Json,
        expression: String
    ): IO[JmespathError, Json] = {
      val io = for {

        jsonNodeExpression <- getExpression(
          expression
        )

        jsonNode <- ZIO
          .attempt(
            circeToJackson(rowData)
          )
          .mapError(e =>
            JmespathError.JsonParseError(
              "circe To Jackson error!",
              Some(e.toString)
            )
          )

        jsonNodeResult <- ZIO
          .attempt(
            jsonNodeExpression.search(
              jsonNode
            )
          )
          .mapError(e =>
            JmespathError.SearchError(
              "JmesPathUtil search error!",
              Some(e.toString)
            )
          )

        jsonResult <- ZIO
          .attempt(
            jacksonToCirce(jsonNodeResult)
          )
          .mapError(e =>
            JmespathError.JsonParseError(
              "Jackson To Circe error!",
              Some(e.toString)
            )
          )

      } yield jsonResult

      io.mapError(e =>
        JmespathError.SearchError(
          "JmesPathUtil search error!",
          Some(e.toString)
        )
      )
    }

    override def searchT[T](rowData: Json, expression: String)(using
        Decoder[T]
    ): IO[JmespathError, T] = {

      for {
        json <- search(
          rowData,
          expression
        )

        res <- ZIO
          .fromEither(json.as[T])
          .mapError(e =>
            JmespathError.JsonParseError(
              "json to T error!",
              Some(e.toString)
            )
          )
      } yield res
    }

    @Override
    def mergeResultsWithExpressions(
        rowData: Json,
        expressions: Seq[String]
    ): IO[JmespathError, Json] = {
      val io = for {

        jsonNode <- ZIO.attempt(
          circeToJackson(rowData)
        )

        jsonResultList <- ZIO.foreach(
          expressions
        )(expression =>
          for {
            jsonNodeExpression <- ZIO.attempt(
              jmesPathRuntime.compile(expression)
            )

            jsonNodeResult <- ZIO.attempt(
              jsonNodeExpression.search(
                jsonNode
              )
            )
            jsonResult <- ZIO.attempt(
              jacksonToCirce(jsonNodeResult)
            )

          } yield jsonResult
        )

        json = jsonResultList
          .filterNot(_.isNull)
          .foldLeft(
            rowData
          )((original, next) => original.deepMerge(next))

      } yield json

      io.mapError(e =>
        JmespathError.SearchError(
          "JmesPathUtil mergeResultsWithExpressions error!",
          Some(e.toString)
        )
      )
    }
  }

  val live: ZLayer[Any, Nothing, Service] = {
    ZLayer {
      val functionRegistry = FunctionRegistry
        .defaultRegistry()
        .extend(
          new StringToJsonFunction()
        )
      val runtimeConfiguration = RuntimeConfiguration
        .builder()
        .withFunctionRegistry(
          functionRegistry
        )
        .build()

      val jmesPathRuntime = new JacksonRuntime(runtimeConfiguration)

      for {
        expressionMapRef <- Ref.make(
          Map.empty[String, Expression[JsonNode]]
        )
        svc <- ZIO.succeed(new ServiceImpl(jmesPathRuntime, expressionMapRef))
      } yield svc

    }
  }

}
