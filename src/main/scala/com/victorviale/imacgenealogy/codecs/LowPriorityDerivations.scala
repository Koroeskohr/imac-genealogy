package com.victorviale.imacgenealogy.codecs

import io.circe.{Decoder, Encoder}

object LowPriorityDerivations {
  import shapeless._

  implicit def encoderValueClass[T <: AnyVal, V](
    implicit g: Lazy[Generic.Aux[T, V :: HNil]],
             e: Encoder[V]): Encoder[T] =
    Encoder.instance { value ⇒
      e(g.value.to(value).head)
    }

  implicit def decoderValueClass[T <: AnyVal, V](
    implicit g: Lazy[Generic.Aux[T, V :: HNil]],
             d: Decoder[V]): Decoder[T] =
    Decoder.instance { cursor ⇒
      d(cursor).map { value ⇒
        g.value.from(value :: HNil)
      }
    }
}
