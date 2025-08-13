package com.dnio.flowwright.core.node

import net.reactivecore.cjs.DocumentValidator

trait NodeValidator {

  val inputValidator: Option[DocumentValidator]

  val outputValidator: Option[DocumentValidator]

}
