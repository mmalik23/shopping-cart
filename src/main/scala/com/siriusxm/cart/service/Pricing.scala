package com.siriusxm.cart.service

import cats.effect.IO
import org.http4s.client.Client
import org.http4s.Request
import org.http4s.Uri
import org.http4s.EntityDecoder
import scala.math.BigDecimal.RoundingMode

object Pricing {
  trait Service {
    def getPrice(productName: String): IO[Option[BigDecimal]]
  }
}

class LivePricing(client: Client[IO]) extends Pricing.Service {
  import io.circe.generic.auto._
  import org.http4s.circe.jsonOf

  case class ResponseBody(title: String, price: BigDecimal)

  def getPrice(productName: String): IO[Option[BigDecimal]] = {
    implicit val responseBodyDecoder: EntityDecoder[IO, ResponseBody] =
      jsonOf[IO, ResponseBody]

    val request = Request[IO](
      uri = Uri.unsafeFromString(
        s"https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/${productName.toLowerCase()}.json"
      )
    )

    (for {
      response <- client.expectOption[ResponseBody](request)
    } yield response.map(_.price.setScale(2, RoundingMode.CEILING)))
      .handleErrorWith(_ => IO.pure(None))
  }
}
