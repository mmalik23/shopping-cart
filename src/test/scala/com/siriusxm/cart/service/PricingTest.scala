package com.siriusxm.cart.service

import cats.effect._
import munit.CatsEffectSuite
import org.http4s.{HttpApp, _}
import org.http4s.client.Client
import org.http4s.dsl.io._

class PricingTest extends CatsEffectSuite {

  test(
    "Can successfully retrieve pricing data, case-insensitive, calling the correct host, path and using https"
  ) {

    val testHttpApp: HttpApp[IO] = createTestApp(price = "2.52")
    val pricing = new LivePricing(createTestClient(testHttpApp))

    pricing.getPrice("CORNFLAKEs").assertEquals(Some(BigDecimal("2.52")))
  }

  test(
    "If the price has less than two decimal places then parse it and set the scale to two"
  ) {

    val testHttpApp: HttpApp[IO] = createTestApp(price = "2.5")
    val pricing = new LivePricing(createTestClient(testHttpApp))

    for {
      maybePrice <- pricing.getPrice("cornflakes")
    } yield {
      assert(maybePrice.nonEmpty)
      assertEquals(maybePrice.get, BigDecimal("2.50"))
      assertEquals(maybePrice.get.scale, 2)
    }
  }

  test(
    "If the price has too many decimal places then parse the value but round up to two decimal places"
  ) {

    val testHttpApp: HttpApp[IO] = createTestApp(price = "2.521")
    val pricing = new LivePricing(createTestClient(testHttpApp))

    for {
      maybePrice <- pricing.getPrice("cornflakes")
    } yield {
      assert(maybePrice.nonEmpty)
      assertEquals(maybePrice.get, BigDecimal("2.53"))
      assertEquals(maybePrice.get.scale, 2)
    }
  }

  test("If no price can be found for product name return None") {

    val notFound: HttpApp[IO] = HttpApp[IO](_ => NotFound())
    val pricingService = new LivePricing(createTestClient(notFound))

    pricingService.getPrice("404").assertEquals(None)
  }

  test("If there is a parsing error then return None") {

    val malformedApp: HttpApp[IO] = HttpApp[IO] {
      case GET -> Root / "mattjanks16" / "shopping-cart-test-data" / "main" / "malformed.json" =>
        Ok("this is not the right json is it")
    }
    val pricing = new LivePricing(createTestClient(malformedApp))

    pricing.getPrice("4XX").assertEquals(None)
  }

  test("If there is a 5X error return None") {

    val serverErrorApp: HttpApp[IO] = HttpApp[IO](_ => InternalServerError())
    val pricing = new LivePricing(createTestClient(serverErrorApp))

    pricing.getPrice("5XX").assertEquals(None)
  }

  private def createTestApp(price: String) = HttpApp[IO] {
    case GET -> Root / "mattjanks16" / "shopping-cart-test-data" / "main" / "cornflakes.json" =>
      Ok(
        s"""
        {
            "title": "Corn Flakes",
            "price": $price
        }
        """
      )
  }

  private def createTestClient(httpApp: HttpApp[IO]): Client[IO] =
    Client { request =>
      (request.uri.scheme, request.uri.authority) match {
        case (Some(scheme), Some(authority))
            if scheme.value == "https" && authority.host.value == "raw.githubusercontent.com" =>
          httpApp.run(request).toResource
        case _ =>
          Resource.pure(
            Response[IO](status = org.http4s.Status.BadRequest)
              .withEntity("Request must use HTTPS and the correct host.")
          )
      }
    }
}
