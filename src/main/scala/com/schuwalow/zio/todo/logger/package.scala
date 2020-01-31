package com.schuwalow.zio.todo

import zio._
import sourcecode._

package object logger extends Logger.Service[Logger] { //Logger.Accessors

  def trace(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.logger.trace(a))

  def debug(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.logger.debug(a))

  def info(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.logger.info(a))

  def warn(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.logger.warn(a))

  def error(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.logger.error(a))

}
