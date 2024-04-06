package com.siriusxm.cart.domain

import scala.math.BigDecimal.RoundingMode

case class Product(name: String, price: BigDecimal)
case class Entry(product: Product, quantity: Int) {
  def total: BigDecimal = product.price * quantity
}

case class ShoppingCart(
    entries: List[Entry] = Nil
) {

  def add(entry: Entry): ShoppingCart = this.copy(
    entries = (entry :: entries)
      .groupMapReduce(_.product)(_.quantity)(_ + _)
      .toList
      .map(Entry.tupled)
  )

  def subTotal: BigDecimal =
    entries.map(_.total).fold(BigDecimal("0.00"))(_ + _)

  def tax: BigDecimal =
    (subTotal * 0.125).setScale(2, RoundingMode.CEILING)

  def totalPayable: BigDecimal = subTotal + tax

}
