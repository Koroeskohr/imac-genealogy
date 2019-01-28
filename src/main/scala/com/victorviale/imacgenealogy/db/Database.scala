package com.victorviale.imacgenealogy
package db

import Config.DatabaseConfig
import cats.effect.IO
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway


case class Database(config: DatabaseConfig) {
  val transactor: IO[HikariTransactor[IO]] = HikariTransactor.newHikariTransactor[IO](config.driver, config.url, config.user, config.password)

  def configure(transactor: HikariTransactor[IO]): IO[Unit] =
    transactor.configure { datasource =>
      IO {
        val fw = new Flyway()
        fw.setDataSource(datasource)
        fw.migrate
        ()
      }
    }
}
