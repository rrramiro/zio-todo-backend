package com.schuwalow.zio.todo.logger

import zio.URIO
import sourcecode._

object Logger {

  trait Service[R] {

    def trace(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit]

    def debug(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit]

    def info(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit]

    def warn(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit]

    def error(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit]
  }
}
