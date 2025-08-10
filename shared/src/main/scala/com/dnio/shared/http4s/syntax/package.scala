package com.dnio.shared.http4s

import com.dnio.shared.http4s.errors.Http4sErrors
import com.dnio.shared.http4s.errors.Http4sErrors.Http4sError
import org.http4s.Uri

package object syntax {

  extension (s: String) {
    def uri: Either[Http4sError, Uri] = Uri
      .fromString(Uri.encode(s))
      .left
      .map(e =>
        Http4sErrors.ParseUriFailure(s"Invalid URI: ${s}, msg: ${e.message}")
      )
  }

}
