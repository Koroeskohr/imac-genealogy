package com.victorviale.imacgenealogy
package db

import cats.effect.IO
import com.victorviale.imacgenealogy.models.Student
import doobie.implicits._
import doobie.util.transactor.Transactor

object StudentReads {
  def getById(id: Int) = sql"SELECT * FROM students WHERE id = $id;"
}

case class StudentDb(transactor: Transactor[IO]) {
  def getById(id: Int): IO[Either[Student.Error, Student]] =
    StudentReads.getById(id).query[Student].option.transact(transactor).map {
      case Some(doc) =>
        Right(doc)
      case None =>
        Left(Student.Error.NotFound)
    }
}
