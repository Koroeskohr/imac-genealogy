package com.victorviale.imacgenealogy
package httpServices

import cats.effect.IO
import com.victorviale.imacgenealogy.models.Student
import db.StudentDb
import io.circe.syntax._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

class StudentService(studentDb: StudentDb) extends Http4sDsl[IO] {
  import codecs.StudentCodec._

  val service: HttpService[IO] = HttpService[IO] {
    case req @ GET -> Root / "students" / IntVar(id) =>
      for {
        studentOrError <- studentDb.getById(id)
        response <- resultToResponse(studentOrError)
      } yield response

  }

  def resultToResponse(result: Either[Student.Error, Student]) = result match {
    case Left(err) => NotFound(err.message)
    case Right(student) => Ok(student.asJson)
  }

}
