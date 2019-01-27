package com.victorviale.imacgenealogy
package db

import cats.effect.IO
import models.Document.{DocumentError, DocumentNotFoundError}
import models.Document
import doobie.implicits._
import doobie.util.transactor.Transactor

object DocumentReads {
  def getById(id: Int) = sql"""SELECT * FROM snippets WHERE document_id = $id ORDER BY order"""
}

case class DocumentDb(transactor: Transactor[IO]) {
  def getById(id: Int): IO[Either[DocumentError, Document]] =
    DocumentReads.getById(id).query[Document].option.transact(transactor).map {
      case Some(doc) =>
        Right(doc)
      case None =>
        Left(DocumentNotFoundError)
    }
}
