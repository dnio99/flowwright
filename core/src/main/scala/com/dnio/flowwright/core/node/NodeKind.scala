package com.dnio.flowwright.core.node

import io.circe.CursorOp
import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.Encoder
import io.circe.HCursor
import io.circe.Json

import scala.util.Try

enum NodeKind:
  case HttpRequest, End;

object NodeKind {
  given Encoder[NodeKind] = (a: NodeKind) => Json.fromString(a.toString)

  given Decoder[NodeKind] = (c: HCursor) =>
    Decoder
      .decodeString(c)
      .flatMap(str =>
        Try(NodeKind.valueOf(str)).toEither.left.map(_ =>
          DecodingFailure("Invalid NodeKind", List(CursorOp.Field(str)))
        )
      )
}
