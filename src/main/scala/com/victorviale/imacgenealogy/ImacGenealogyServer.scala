package com.victorviale.imacgenealogy

import cats.effect.IO
import db.{Database, DocumentDb}
import httpServices.{DocumentsService, HelloWorldService}
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object SnippetGirlServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] = ServerStream.stream
}

object ServerStream {
  def stream(implicit ec: ExecutionContext): Stream[IO, StreamApp.ExitCode] =
    for {
      conf <- Stream.eval(Config.load)
      db = Database(conf.db)
      transactor <- Stream.eval(db.transactor)
      _ <- Stream.eval(db.configure(transactor))
      documentDb = DocumentDb(transactor)
      documentsService = new DocumentsService(documentDb).service
      exitCode <- BlazeBuilder[IO]
        .bindHttp(conf.server.port, conf.server.host)
        .mountService(documentsService, "/")
        .mountService(new HelloWorldService[IO].service, "/")
        .serve
    } yield exitCode

}
