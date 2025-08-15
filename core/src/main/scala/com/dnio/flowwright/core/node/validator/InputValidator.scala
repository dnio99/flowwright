package com.dnio.flowwright.core.node.validator

import net.reactivecore.cjs.DocumentValidator

trait InputValidator {

  val inputValidator: Option[DocumentValidator]

}
