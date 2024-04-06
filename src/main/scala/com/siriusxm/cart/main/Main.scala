package com.siriusxm.cart.main

import cats.effect.IOApp
import cats.effect.IO
import com.siriusxm.cart.service.LivePricing
import com.siriusxm.cart.service.LiveCartManagementService
import com.siriusxm.cart.domain.ShoppingCart
import org.http4s.ember.client.EmberClientBuilder

object Main extends IOApp.Simple {
  def run: IO[Unit] = {

    val httpClient = EmberClientBuilder
      .default[IO]
      .build

    httpClient.use { client =>
      val pricingService = new LivePricing(client)
      val catalogueService = new LiveCartManagementService(pricingService)
      for {
        cart <- catalogueService.addProductToCart(
          ShoppingCart(),
          "cornflakes",
          2
        )
        finalCart <- catalogueService.addProductToCart(
          cart,
          "weetabix",
          1
        )
        _ <- IO(println("Cart Contents:"))
        _ <- IO(println(finalCart.entries.foreach(println)))
        _ <- IO(println(s"Subtotal: ${finalCart.subTotal}"))
        _ <- IO(println(s"Tax: ${finalCart.tax}"))
        _ <- IO(println(s"Total Payable: ${finalCart.totalPayable}"))
      } yield ()
    }
  }

}
