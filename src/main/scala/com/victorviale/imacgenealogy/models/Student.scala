package com.victorviale.imacgenealogy
package models

import Student._

case class Student(
  fullName: FullName
)

object Student {
  case class Id(value: Int) extends AnyVal
  case class FullName(value: String) extends AnyVal

  sealed abstract class Error(val message: String)

  object Error {
    case object NotFound extends Error("Not found")
  }
}
