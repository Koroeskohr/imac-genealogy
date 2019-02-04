package com.victorviale.imacgenealogy

import java.sql.SQLException

import org.specs2._
import cats.effect.IO
import com.typesafe.config.ConfigFactory
import com.victorviale.imacgenealogy.db.Database
import doobie.hikari.HikariTransactor
import org.specs2.specification.BeforeAll

trait DbSpec extends mutable.Specification with BeforeAll {
  import doobie._
  import doobie.implicits._

  this.stopOnFail

  private val config = ConfigFactory.load("application.test")

  private val dbConf: Config.DatabaseConfig = Config.DatabaseConfig(
    config.getString("db.driver"),
    config.getString("db.url"),
    config.getInt("db.port"),
    config.getString("db.user"),
    config.getString("db.password")
  )

  private val db = Database(dbConf)

  private val transactor = HikariTransactor.newHikariTransactor[IO](
    dbConf.driver,
    dbConf.url,
    dbConf.user,
    dbConf.password
  ).unsafeRunSync()

  val xa = Transactor.after.set(transactor, HC.rollback)

  private def checkDbConnection: Either[SQLException, Int] =
    sql"SELECT 1".query[Int].unique.transact(xa).attemptSql.unsafeRunSync()

  def beforeAll = {
    checkDbConnection match {
      case Left(err) => this.ko(s"Connection to database could not be established : \n${err.getMessage}");
      case Right(_) => this.ok("Connection to database ok")
    }

    db.configure(transactor).unsafeRunSync()
  }

  def transactionally[A](run: ConnectionIO[A]): IO[A] =
    run.transact(xa)

}
