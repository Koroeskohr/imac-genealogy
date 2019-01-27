package com.victorviale.imacgenealogy.models

case class Promotion(
  year: Promotion.Year
)

object Promotion {
  case class Year(value: Int) extends AnyVal
}
