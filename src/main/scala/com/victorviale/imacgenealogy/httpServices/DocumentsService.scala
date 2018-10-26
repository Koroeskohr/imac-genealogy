package com.victorviale.imacgenealogy
package httpServices

import cats.effect.IO
import db.DocumentDb
import io.circe.Json
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class DocumentsService(documentDb: DocumentDb) extends Http4sDsl[IO] {
  val service: HttpService[IO] = HttpService[IO] {
    case req @ GET -> Root / "documents" / IntVar(id) =>
      for {
        docOrError <- documentDb.getById(id)
        response <- resultToResponse(docOrError)
      } yield response

  }

  def resultToResponse(result: Either[DocumentError, Document]) = result match {
    case Left(err) => NotFound(err.message)
    case Right(doc) => Ok(doc.asJson)
  }

}
