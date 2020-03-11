package shop.modules

import cats.effect._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware._
import org.http4s.server.Router
import scala.concurrent.duration._
import shop.http.routes._

object HttpApi {
  def make[F[_]: Concurrent: Timer](
      algebras: Algebras[F]
  ): F[HttpApi[F]] =
    Sync[F].delay(
      new HttpApi[F](
        algebras
      )
    )
}

final class HttpApi[F[_]: Concurrent: Timer] private (
    algebras: Algebras[F]
) {

  // Open routes
  private val healthRoutes = new HealthRoutes[F](algebras.healthCheck).routes

  // Combining all the http routes
  private val openRoutes: HttpRoutes[F] =
    healthRoutes

  private val routes: HttpRoutes[F] = Router(
    version.v1 -> openRoutes
  )

  private val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logHeaders = true, logBody = true)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logHeaders = true, logBody = true)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
