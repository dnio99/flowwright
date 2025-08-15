package com.dnio.flowwright.core.node.validator

import net.reactivecore.cjs.DocumentValidator

trait OutputValidator {

  val outputValidator: Option[DocumentValidator]

}
