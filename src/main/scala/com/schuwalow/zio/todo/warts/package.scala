package com.schuwalow.zio.todo

import cats.Show
import zio.Fiber
import com.github.ghik.silencer.silent

package object warts {

  @silent("wartremover:ToString")
  implicit val catsShowForFiberId: Show[Fiber.Id] = (t: Fiber.Id) => t.toString

}
