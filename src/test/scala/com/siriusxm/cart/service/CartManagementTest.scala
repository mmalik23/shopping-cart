package com.siriusxm.cart.service

import munit.CatsEffectSuite
import com.siriusxm.cart.domain.ShoppingCart
import com.siriusxm.cart.stubs.StubPricingService
import com.siriusxm.cart.domain.Entry
import com.siriusxm.cart.domain

class CartManagementTest extends CatsEffectSuite {

  test("Can add multiple products to a cart and aggregate") {

    val firstProductName = "First"
    val secondProductName = "Second"

    val pricingService = StubPricingService { productName =>
      if (productName == "First") Some(BigDecimal("5.00"))
      else if (productName == "Second") Some(BigDecimal("3.00"))
      else None
    }

    val cartManagementService = new LiveCartManagementService(pricingService)

    val expectedCart = ShoppingCart(
      List(
        Entry(
          product = domain.Product(secondProductName, BigDecimal("3.00")),
          quantity = 3
        ),
        Entry(
          product = domain.Product(firstProductName, BigDecimal("5.00")),
          quantity = 3
        )
      )
    )

    for {
      modifiedCart <- cartManagementService.addProductToCart(
        existingCart = ShoppingCart(),
        productName = firstProductName,
        quantity = 2
      )
      modifiedCart2 <- cartManagementService.addProductToCart(
        existingCart = modifiedCart,
        productName = secondProductName,
        quantity = 3
      )
      modifiedCart3 <- cartManagementService.addProductToCart(
        existingCart = modifiedCart2,
        productName = firstProductName,
        quantity = 1
      )

    } yield assertEquals(modifiedCart3, expectedCart)
  }

  test(
    "If a product is adding to the cart for which no price exists, return the existing cart"
  ) {
    val pricingService = StubPricingService(_ => None)
    val catalogueService = new LiveCartManagementService(pricingService)
    val product = "i-don't-exist"

    val existingCart =
      ShoppingCart(
        List(
          Entry(
            product = domain.Product("whoop", BigDecimal("3.00")),
            quantity = 3
          )
        )
      )

    catalogueService
      .addProductToCart(existingCart, product, 2)
      .assertEquals(existingCart)
  }
}
