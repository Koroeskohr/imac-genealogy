package com.victorviale.imacgenealogy

import net.ceedubs.ficus.Ficus._
import com.typesafe.config.ConfigFactory
import Config._
import cats.effect.IO

case class Config(db: DatabaseConfig, server: ServerConfig)

object Config {
  case class DatabaseConfig(driver: String, url: String, port: Int, user: String, password: String)
  case class ServerConfig(host: String, port: Int)

  def load = {
    import net.ceedubs.ficus.readers.ArbitraryTypeReader._
    IO {
      val conf = ConfigFactory.load()

      val db: DatabaseConfig = conf.as[DatabaseConfig]("db")
      val server: ServerConfig = conf.as[ServerConfig]("app")

      Config(db, server)
    }
  }
}
