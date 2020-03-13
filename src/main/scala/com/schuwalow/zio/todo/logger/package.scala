package com.schuwalow.zio.todo

import zio._
import sourcecode._

package object logger extends Logger.Service[Has[Logger.Service[Any]]] {
  type Logger = Has[Logger.Service[Any]]

  def trace(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.get.trace(a))

  def debug(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.get.debug(a))

  def info(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.get.info(a))

  def warn(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.get.warn(a))

  def error(
    a: String
  )(implicit
    line: Line,
    file: File
  ): URIO[Logger, Unit] =
    ZIO.accessM(_.get.error(a))

}
