package com.schuwalow.zio.todo
import zio._
import zio.stream._
import zio.interop.reactivestreams.publisherToStream
import fs2.interop.reactivestreams.StreamOps
import zio.interop.catz._

package object stream {

  implicit class Fs2StreamOps[R, A](stream: fs2.Stream[RIO[R, *], A]) {

    def toZStream: ZStream[R, Nothing, A] =
      ZStream
        .fromEffect(ZIO.runtime[R].map { implicit runtime =>
          stream.toUnicastPublisher.toStream().catchAll(_ => ZStream.empty)
        })
        .flatten
  }

}
