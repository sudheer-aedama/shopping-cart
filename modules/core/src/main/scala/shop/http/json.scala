package shop.http

import cats.Applicative
import dev.profunktor.auth.jwt.JwtToken
import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import shop.domain.brand._
import shop.domain.healthcheck.AppStatus
import squants.market._

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}

private[http] trait JsonCodecs {

  // ----- Overriding some Coercible codecs ----
  implicit val brandParamDecoder: Decoder[BrandParam] =
    Decoder.forProduct1("name")(BrandParam.apply)

  // ----- Coercible codecs -----
  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  // ----- Domain codecs -----

  implicit val brandDecoder: Decoder[Brand] = deriveDecoder[Brand]
  implicit val brandEncoder: Encoder[Brand] = deriveEncoder[Brand]

  implicit val moneyDecoder: Decoder[Money] =
    Decoder[BigDecimal].map(USD.apply)

  implicit val moneyEncoder: Encoder[Money] =
    Encoder[BigDecimal].contramap(_.amount)

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)

  implicit val appStatusEncoder: Encoder[AppStatus] = deriveEncoder[AppStatus]

}
