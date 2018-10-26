val Http4sVersion = "0.18.12"
val Specs2Version = "4.2.0"
val LogbackVersion = "1.2.3"
val DoobieVersion = "0.5.3"
val FicusVersion = "1.4.3"
val FlywayVersion = "5.1.0"

lazy val root = (project in file("."))
  .settings(
    organization := "com.victorviale",
    name := "imac-genealogy",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "org.specs2"      %% "specs2-core"         % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion,
      "com.iheart"      %% "ficus"               % FicusVersion,
      "org.flywaydb"    %  "flyway-core"         % FlywayVersion,
      "org.tpolecat"    %% "doobie-core"         % DoobieVersion,
      "org.tpolecat"    %% "doobie-hikari"       % DoobieVersion, // HikariCP transactor.
      "org.tpolecat"    %% "doobie-postgres"     % DoobieVersion, // Postgres driver 42.2.2 + type mappings.
      "org.tpolecat"    %% "doobie-specs2"       % DoobieVersion, // Specs2 support for typechecking statements.

    )
  )

