package com.siriusxm.cart.domain

import munit.FunSuite

class ShoppingCartTest extends FunSuite {

  test("If the shopping cart is empty all the values must total 0.00") {
    val emptyCart = ShoppingCart()

    assertEquals(emptyCart.subTotal, BigDecimal("0.00"))
    assertEquals(emptyCart.tax, BigDecimal("0.00"))
    assertEquals(emptyCart.totalPayable, BigDecimal("0.00"))

  }

  test(
    "If we have multiple entries in the shopping cart, then calculate the correct price"
  ) {
    val entry = Entry(Product("product", BigDecimal("1.00")), 2)
    val cart =
      ShoppingCart()
        .add(entry)
        .add(entry)

    assertEquals(cart.subTotal, BigDecimal("4.00"))
    assertEquals(cart.tax, BigDecimal("0.50"))
    assertEquals(cart.totalPayable, BigDecimal("4.50"))
    assertEquals(cart.entries, List(entry.copy(quantity = entry.quantity * 2)))

  }

  test(
    "If the price of the product has changed then consider it a distinct entry"
  ) {
    val entry1 = Entry(Product("product", BigDecimal("0.50")), 2)
    val entry2 = Entry(Product("product", BigDecimal("1.50")), 2)
    val cart =
      ShoppingCart()
        .add(entry1)
        .add(entry2)

    assertEquals(cart.entries.length, 2)
  }

  test(
    "If the total value of the basket is 0.01, then tax must be 0.01 because it is rounded up"
  ) {
    val productPrice = BigDecimal("0.01")
    val quantity = 1
    val cart =
      ShoppingCart().add(Entry(Product("product", productPrice), quantity))

    assertEquals(cart.subTotal, productPrice)
    assertEquals(cart.tax, productPrice)
  }
}
