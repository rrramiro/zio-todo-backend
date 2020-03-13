package com.schuwalow.zio.todo

import cats.Show
import zio.Fiber

package object warts {

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  implicit val catsShowForFiberId: Show[Fiber.Id] = (t: Fiber.Id) => t.toString

}
