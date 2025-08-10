package com.dnio.flowwright.core.template

import com.dnio.flowwright.core.errors.WorkFlowErrors
import WorkFlowErrors.WorkFlowError
import com.dnio.jmespath.JmespathZio
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder, Json}
import zio.{IO, Ref, ZIO}

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
        case _ => json.noSpaces.stripPrefix("\"").stripSuffix("\"")
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
  ): ZIO[JmespathZio.Service, WorkFlowError, Map[String, Json]] = {

    val searchExpression = placeholders.zipWithIndex
      .map { case (placeholder, index) =>
        s"${lookupKey(index)}:${placeholder}"
      }
      .mkString("{", ",", "}")

    (for {
      jmespath <- ZIO.service[JmespathZio.Service]

      data <- dataRef.get.map(_.asJson)

      // todo: delete debug log
      _ <- ZIO.logInfo(
        s"data: ${data.spaces2}, searchExpression: ${searchExpression}"
      )
      res <- jmespath.searchT[Map[String, Json]](
        data,
        searchExpression
      )
    } yield res).mapError(e =>
      WorkFlowErrors.TemplateParseError(
        s"Failed to resolve template placeholders: ${e.toString}",
        Some(e.message)
      )
    )

  }

  private def interpolate[T](template: Json, data: Map[String, Json])(using
      Decoder[T]
  ): IO[WorkFlowError, T] = {

    val res = template match {
      case _ if template.isString =>
        Right(Json.fromString(data.foldLeft(template.asString.getOrElse("")) {
          case (acc, (key, value)) =>
            acc.replace(s"__{$key}__", JsonUtil.toString(value))
        }))
      case _ if template.isObject =>
      case _                      =>
        Left(
          WorkFlowErrors.TemplateParseError(
            s"Template is not a valid JSON object or string: ${template.noSpaces}",
            Some(template.noSpaces)
          )
        )
    }

    val t = data.foldLeft(template) { case (acc, (key, value)) =>
      acc.replace(s"__{$key}__", JsonUtil.toString(value))
    }
    ZIO
      .fromEither(decode[T](t))
      .mapError(e =>
        WorkFlowErrors.TemplateParseError(
          s"Failed to decode template: ${e.getMessage}",
          Some(e.getMessage)
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

  def handle[T](dataRef: Ref[Map[String, Json]], template: T)(using
      Encoder[T],
      Decoder[T]
  ): ZIO[JmespathZio.Service, WorkFlowError, T] = {
    val templateStr = template.asJson.noSpaces
    val placeholders = extractPlaceholders(templateStr)
    placeholders match {
      case Nil => ZIO.succeed(template)
      case _   => {
        for {
          data <- lookup(dataRef, placeholders)

          templateJson <- ZIO
            .fromEither(parse(prepareTemplate(templateStr, placeholders)))
            .mapError(e =>
              WorkFlowErrors.TemplateParseError(
                s"Failed to parse template: ${e.getMessage}",
                Some(e.getMessage)
              )
            )

          _ <- ZIO.logInfo(
            s"Resolved template: ${template}, with placeholders: ${placeholders.mkString(", ")}"
          )
          res <- interpolate(templateJson, data)

        } yield res
      }
    }

  }

}
