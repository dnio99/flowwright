package com.dnio.flowwright.core.test_layer

import com.dnio.jmespath.JmespathZio
import zio.ZLayer

object JmespathLayer {

  val live: ZLayer[Any, Nothing, JmespathZio.Service] = JmespathZio.live

}
