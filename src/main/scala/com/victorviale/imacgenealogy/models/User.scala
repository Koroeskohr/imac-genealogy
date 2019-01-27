package com.victorviale.imacgenealogy
package models

import java.time.LocalDateTime

case class User(
  email: User.Email,
  relatedStudent: Option[Student],
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime)

object User {
  case class Email(value: String) extends AnyVal
  case class Password(value: String) extends AnyVal {
    override def toString: String = "<REDACTED>"
  }
}


