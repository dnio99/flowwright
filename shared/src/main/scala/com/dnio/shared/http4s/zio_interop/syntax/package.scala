package com.dnio.shared.http4s.zio_interop

import com.dnio.shared.http4s.Http4sClient
import com.dnio.shared.http4s.errors.Http4sErrors
import com.dnio.shared.http4s.syntax._
import org.http4s.EntityDecoder
import org.http4s.Request
import org.http4s.Uri
import org.http4s.client.Client
import zio.Task
import zio.ZIO

package object syntax {

  extension (s: String) {

    def get[T](using
        EntityDecoder[Task, T],
        Http4sClient.LoggerConfig
    ): ZIO[Client[Task], Http4sErrors.Http4sError, T] = {
      for {
        uri <- ZIO.fromEither(s.uri)
        res <- uri.get[T]
      } yield res
    }

  }

  extension (uri: Uri) {

    def get[T](using
        EntityDecoder[Task, T],
        Http4sClient.LoggerConfig
    ): ZIO[Client[Task], Http4sErrors.Http4sError, T] = {
      ZioHttp4sClient.request[T](
        Request[Task](
          uri = uri
        )
      )
    }

  }
}
