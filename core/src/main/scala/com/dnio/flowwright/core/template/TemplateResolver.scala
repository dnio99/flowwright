package com.dnio.flowwright.core.template

import com.dnio.flowwright.core.errors.WorkflowErrors
import com.dnio.flowwright.core.errors.WorkflowErrors.WorkflowError
import com.dnio.flowwright.core.workflow.WorkflowContextData
import com.dnio.jmespath.JmespathZio
import io.circe.Decoder
import io.circe.Encoder
import io.circe.Json
import io.circe.JsonObject
import io.circe.parser._
import io.circe.syntax._
import zio.Ref
import zio.ZIO

import scala.util.matching.Regex

object TemplateResolver {

  private object JsonUtil {

    private val NULL = "null"

    def toString(json: Json): String = {
      json match {
        case _ if json.isString => json.asString.getOrElse(NULL)
        case _ if json.isNumber => json.asNumber.map(_.toString).getOrElse(NULL)
        case _ if json.isBoolean =>
          json.asBoolean.map(_.toString).getOrElse(NULL)
        case _ if json.isNull => NULL
        case _                => json.noSpaces
      }
    }

  }

  private val PLACE_HOLDER_REGEX: Regex = """__\{([^}]+)}__""".r

  private def extractPlaceholders(template: String): Seq[String] = {
    PLACE_HOLDER_REGEX
      .findAllMatchIn(template)
      .map(_.group(1))
      .toSeq
  }

  private def lookupKey: Int => String = n => s"index_$n"

  private def lookup(
      dataRef: Ref[Map[String, Json]],
      placeholders: Seq[String]
  ): ZIO[JmespathZio.Service, WorkflowError, Map[String, Json]] = {

    val searchExpression = placeholders.zipWithIndex
      .map { case (placeholder, index) =>
        s"${lookupKey(index)}:${placeholder}"
      }
      .mkString("{", ",", "}")

    (for {
      jmespath <- ZIO.service[JmespathZio.Service]

      data <- dataRef.get.map(_.asJson)

      res <- jmespath.searchT[Map[String, Json]](
        data,
        searchExpression
      )
    } yield res).mapError(e =>
      WorkflowErrors.TemplateParseError(
        s"Failed to resolve template placeholders: ${e.toString}",
        Some(e.message)
      )
    )

  }

  private def prepareTemplate(
      originalTemplate: String,
      placeholders: Seq[String]
  ): String = {
    placeholders.zipWithIndex.foldLeft(
      originalTemplate
    ) { case (template, (placeholder, index)) =>
      template.replace(s"__{$placeholder}__", s"__{${lookupKey(index)}}__")
    }
  }

  private def interpolateObject(
      data: Map[String, Json],
      template: JsonObject
  ) = {

    def loop(json: Json): Json = {
      json match {
        case json if json.isString =>
          json.asString
            .flatMap(str => {
              Option(PLACE_HOLDER_REGEX.findAllIn(str).group(1))
            })
            .map(str => data.getOrElse(str, Json.Null))
            .getOrElse(Json.Null)
        case json if json.isObject =>
          Json.fromJsonObject(
            json.asObject.getOrElse(JsonObject.empty).mapValues(v => loop(v))
          )
        case value => value
      }
    }

    loop(Json.fromJsonObject(template))
  }

  private def interpolateString(
      data: Map[String, Json],
      template: String
  ): Json = {
    val t = data.foldLeft(template) { case (acc, (key, value)) =>
      acc.replace(s"__{$key}__", JsonUtil.toString(value))
    }
    Json.fromString(t);
  }

  def handle[T, A](dataRef: WorkflowContextData, template: A)(using
      Decoder[T],
      Encoder[A]
  ): ZIO[JmespathZio.Service, WorkflowError, T] = {
    val templateJson = template.asJson
    val templateStr = templateJson.noSpaces
    val placeholders = extractPlaceholders(templateStr)
    placeholders match {
      case Nil =>
        ZIO
          .fromEither(templateJson.as[T])
          .mapError(e =>
            WorkflowErrors.TemplateParseError(
              s"Failed to decode template: ${e.getMessage}",
              Some(e.getMessage)
            )
          )
      case _ => {
        for {
          data <- lookup(dataRef, placeholders)

          templateJson <- ZIO
            .fromEither(parse(prepareTemplate(templateStr, placeholders)))
            .mapError(e =>
              WorkflowErrors.TemplateParseError(
                s"Failed to parse template: ${e.getMessage}",
                Some(e.getMessage)
              )
            )

          json <- templateJson match {
            case _
                if templateJson.isObject && templateJson.asObject.isDefined =>
              ZIO.succeed(
                interpolateObject(data, templateJson.asObject.get)
              )
            case _
                if templateJson.isString && templateJson.asString.isDefined =>
              ZIO.succeed(
                interpolateString(data, templateJson.asString.get)
              )
            case _ =>
              ZIO.fail(
                WorkflowErrors.TemplateParseError(
                  s"Template is not a valid JSON object or string: ${templateJson.noSpaces}",
                  Some(templateJson.noSpaces)
                )
              )
          }

          res <- ZIO
            .fromEither(json.as[T])
            .mapError(e =>
              WorkflowErrors.TemplateParseError(
                s"Failed to decode template: ${e.getMessage}",
                Some(e.getMessage)
              )
            )

        } yield res
      }
    }

  }

}
