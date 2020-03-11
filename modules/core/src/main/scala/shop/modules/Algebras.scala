package shop.modules

import cats.Parallel
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.algebra.RedisCommands
import shop.algebras._
import skunk._

object Algebras {
  def make[F[_]: Concurrent: Parallel: Timer](
      redis: RedisCommands[F, String, String],
      sessionPool: Resource[F, Session[F]]
  ): F[Algebras[F]] =
    for {
      brands <- LiveBrands.make[F](sessionPool)
      health <- LiveHealthCheck.make[F](sessionPool, redis)
    } yield new Algebras[F](brands, health)
}

final class Algebras[F[_]] private (
    val brands: Brands[F],
    val healthCheck: HealthCheck[F]
)
