package com.jisantuc.tracingdemos.database

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import scala.util.Properties

object Config {
  var jdbcDriver: String = "org.postgresql.Driver"

  val jdbcNoDBUrl: String =
    Properties.envOrElse("POSTGRES_URL", "jdbc:postgresql://database.service.internal/")

  val jdbcDBName: String =
    Properties.envOrElse("POSTGRES_NAME", "tracingdemos")
  val jdbcUrl: String = jdbcNoDBUrl + jdbcDBName
  val dbUser: String  = Properties.envOrElse("POSTGRES_USER", "tracingdemos")

  val dbPassword: String =
    Properties.envOrElse("POSTGRES_PASSWORD", "tracingdemos")

  val dbStatementTimeout: String =
    Properties.envOrElse("POSTGRES_STATEMENT_TIMEOUT", "30000")

  val dbMaximumPoolSize: Int =
    Properties.envOrElse("POSTGRES_DB_POOL_SIZE", "5").toInt

  val hikariConfig = new HikariConfig()
  hikariConfig.setPoolName("tracingdemos-pool")
  hikariConfig.setMaximumPoolSize(dbMaximumPoolSize)
  hikariConfig.setConnectionInitSql(s"SET statement_timeout = ${dbStatementTimeout};")
  hikariConfig.setJdbcUrl(jdbcUrl)
  hikariConfig.setUsername(dbUser)
  hikariConfig.setPassword(dbPassword)
  hikariConfig.setDriverClassName(jdbcDriver)

  val hikariDS = new HikariDataSource(hikariConfig)
}
