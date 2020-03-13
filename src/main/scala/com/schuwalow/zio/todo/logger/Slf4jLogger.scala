package com.schuwalow.zio.todo.logger

import cats.syntax.flatMap._
import cats.syntax.show._
import cats.instances.int._
import zio.interop.catz._
import zio._
import org.slf4j.{ LoggerFactory, Logger => SLogger }
import com.schuwalow.zio.todo.warts._
import sourcecode._

object Slf4jLogger {

  def withSlf4jLogger(name: String) = ZLayer.succeed[Logger.Service[Any]](
    new Slf4jLogger.Service[Any](
      LoggerFactory.getLogger(name),
      _.value.split("/").last
    )
  )

  class Service[R](
    inner: SLogger,
    showFile: File => String)
      extends Logger.Service[R] {

    def trace(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] =
      withFiberContext(inner.isTraceEnabled)(a)(inner.trace(_: String))

    def debug(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] =
      withFiberContext(inner.isDebugEnabled)(a)(inner.debug(_: String))

    def info(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] =
      withFiberContext(inner.isInfoEnabled)(a)(inner.info(_: String))

    def warn(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] =
      withFiberContext(inner.isWarnEnabled)(a)(inner.warn(_: String))

    def error(
      a: String
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] =
      withFiberContext(inner.isErrorEnabled)(a)(inner.error(_: String))

    private def withFiberContext(
      condition: => Boolean
    )(
      a: String
    )(
      f: String => Unit
    )(implicit
      line: Line,
      file: File
    ): URIO[R, Unit] =
      ZIO.descriptorWith(
        desc =>
          ZIO
            .effectTotal(condition)
            .ifM(
              ZIO.effectTotal(
                f(
                  s"${showFile(file)}:${line.value.show} - <${desc.id.show}> - $a"
                )
              ),
              ZIO.unit
            )
      )
  }
}
