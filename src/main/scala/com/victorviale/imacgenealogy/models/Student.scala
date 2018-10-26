package com.victorviale.imacgenealogy
package models

import Student._

case class Student(
  fullName: FullName
)

object Student {
  case class FullName(value: String) extends AnyVal
}
