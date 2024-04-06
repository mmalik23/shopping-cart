package com.siriusxm.cart.main

import munit.CatsEffectSuite
import com.siriusxm.cart.stubs.StubPricingService
import com.siriusxm.cart.service.LiveCartManagementService
import com.siriusxm.cart.domain.ShoppingCart

class IntegrationTest extends CatsEffectSuite {

  test("Can correctly run the example in README.md") {

    val stubPricingService = StubPricingService {
      case "cornflakes" => Some(BigDecimal("2.52"))
      case "weetabix"   => Some(BigDecimal("9.98"))
      case _            => None
    }

    val cartManagementService = new LiveCartManagementService(stubPricingService)

    for {
      cart <- cartManagementService.addProductToCart(
        ShoppingCart(),
        "cornflakes",
        2
      )
      finalCart <- cartManagementService.addProductToCart(
        cart,
        "weetabix",
        1
      )
    } yield {
      assertEquals(finalCart.subTotal, BigDecimal("15.02"))
      assertEquals(finalCart.tax, BigDecimal("1.88"))
      assertEquals(finalCart.totalPayable, BigDecimal("16.90"))
    }
  }
}
