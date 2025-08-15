package com.dnio.flowwright.core.node.validator

import net.reactivecore.cjs.DocumentValidator

trait InputMandatoryValidator extends InputValidator {
  val inputValidator: Some[DocumentValidator]
}
