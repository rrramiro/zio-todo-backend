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
          stream.toUnicastPublisher().toStream().catchAll(_ => ZStream.empty)
        })
        .flatMap(identity)

    def toZStream2: ZStream[R, Nothing, A] =
      ZStream.managed {
        {
          for {
            queue <- Queue.bounded[Take[Throwable, A]](1).toManaged(_.shutdown)
            _ <- ZIO
                  .runtime[R]
                  .toManaged_
                  .flatMap { implicit runtime =>
                    (stream.evalTap(a => queue.offer(Take.Value(a))) ++ fs2.Stream
                      .eval(queue.offer(Take.End)))
                      .handleErrorWith(
                        e =>
                          fs2.Stream
                            .eval(queue.offer(Take.Fail(Cause.fail(e))))
                            .drain
                      )
                      .compile
                      .resource
                      .drain
                      .toManaged
                  }
                  .fork
          } yield ZStream.fromQueue(queue).unTake
        }
      }.flatMap(_.catchAll(_ => ZStream.empty))
  }

}
