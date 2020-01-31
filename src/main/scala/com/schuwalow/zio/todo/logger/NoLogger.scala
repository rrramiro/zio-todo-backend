package com.schuwalow.zio.todo.logger

import zio._
import zio.macros.delegate._
import sourcecode._

object NoLogger {

  def withNoLogger: EnrichWith[Logger] =
    enrichWith[Logger](new Logger {
      val logger: NoLogger.Service[Any] = new NoLogger.Service[Any] {}
    })

  trait Service[R] extends Logger.Service[R] {

    override def trace(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] = ZIO.unit

    override def debug(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] = ZIO.unit

    override def info(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] = ZIO.unit

    override def warn(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] = ZIO.unit

    override def error(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] = ZIO.unit
  }
}
