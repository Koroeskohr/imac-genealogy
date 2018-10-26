package com.victorviale.imacgenealogy.codecs

import com.victorviale.imacgenealogy.models.Snippet
import io.circe.{Encoder, Json}
import io.circe.syntax._

object SnippetEncoder {
  implicit def snippetEncoder[A](implicit en: Encoder[A]) = new Encoder[Snippet[A]] {
    def apply(s: Snippet[A]) = Json.obj(
      ("type", Json.fromString("dis my type")),
      ("body", s.body.asJson)
    )
  }
}
