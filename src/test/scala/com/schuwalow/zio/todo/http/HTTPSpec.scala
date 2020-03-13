package com.schuwalow.zio.todo.http

import io.circe.Json
import org.http4s._
import org.http4s.circe._
import zio.test.Assertion._
import zio.test._
import zio.interop.catz._
import zio._

object HTTPSpec {

  def request[F[_]](
    method: Method,
    uri: String
  ): Request[F] =
    Request(method = method, uri = Uri.unsafeFromString(uri))

  def checkRequest[R, A](
    actual: RIO[R, Response[RIO[R, *]]],
    expectedStatus: Status,
    expectedBody: Option[A]
  )(implicit
    ev: EntityDecoder[RIO[R, *], A]
  ): RIO[R, TestResult] =
    for {
      response <- actual
      bodyResult <- expectedBody
                     .fold[RIO[R, TestResult]](
                       assertM(response.bodyAsText.compile.toVector)(isEmpty)
                     )(expected => assertM(response.as[A])(equalTo(expected)))
      statusResult = assert(response.status)(equalTo(expectedStatus))
    } yield bodyResult && statusResult

  def checkRequestJson[R, A](
    actual: RIO[R, Response[RIO[R, *]]],
    expectedStatus: Status,
    expectedBody: Json
  ): RIO[R, TestResult] =
    checkRequest(actual, expectedStatus, Some(expectedBody))

  def checkRequestRaw[R, A](
    actual: RIO[R, Response[RIO[R, *]]],
    expectedStatus: Status,
    expectedBody: String
  ): RIO[R, TestResult] =
    checkRequest(actual, expectedStatus, Some(expectedBody))

}
