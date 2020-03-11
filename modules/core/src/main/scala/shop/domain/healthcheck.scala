package shop.domain

import io.estatico.newtype.macros._

object healthcheck {
  @newtype case class RedisStatus(value: Boolean)
  @newtype case class PostgresStatus(value: Boolean)

  case class AppStatus(
      redisStatus: RedisStatus,
      postgresStatus: PostgresStatus
  )
}
