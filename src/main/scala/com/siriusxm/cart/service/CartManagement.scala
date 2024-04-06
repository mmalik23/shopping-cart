package com.siriusxm.cart.service

import cats.effect.IO
import com.siriusxm.cart.domain.Entry
import com.siriusxm.cart.domain.Product
import com.siriusxm.cart.domain.ShoppingCart

object CartManagement {
  trait Service {
    def addProductToCart(
        existingCart: ShoppingCart,
        productName: String,
        quantity: Int
    ): IO[ShoppingCart]
  }
}

class LiveCartManagementService(
    pricingService: Pricing.Service
) extends CartManagement.Service {

  def addProductToCart(
      existingCart: ShoppingCart,
      productName: String,
      quantity: Int
  ): IO[ShoppingCart] = for {
    maybePrice <- pricingService.getPrice(productName)
  } yield maybePrice
    .map(price => Entry(Product(productName, price), quantity))
    .map(existingCart.add)
    .getOrElse(existingCart)
}
