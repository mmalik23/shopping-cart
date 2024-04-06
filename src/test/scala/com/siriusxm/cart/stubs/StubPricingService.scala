package com.siriusxm.cart.stubs

import com.siriusxm.cart.service.Pricing
import cats.effect.IO

case class StubPricingService(f: String => Option[BigDecimal])
    extends Pricing.Service {
  def getPrice(productName: String): IO[Option[BigDecimal]] =
    IO.pure(f(productName))
}
