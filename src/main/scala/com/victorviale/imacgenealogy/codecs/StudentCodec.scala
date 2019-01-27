package com.victorviale.imacgenealogy.codecs

import com.victorviale.imacgenealogy.models.Student
import io.circe.{Encoder, Json}
import io.circe.syntax._

object StudentCodec {
  import LowPriorityDerivations._

  implicit val studentEncoder: Encoder[Student] = new Encoder[Student] {
    def apply(s: Student) = Json.obj(
      ("fullName", s.fullName.asJson)
    )
  }
}
