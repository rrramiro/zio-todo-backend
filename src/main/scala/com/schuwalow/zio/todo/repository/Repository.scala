package com.schuwalow.zio.todo.repository

import com.schuwalow.zio.todo.domain._
import zio._
import zio.stream.ZStream

object Repository {

  trait Service[R] {
    def getAll: URIO[R, List[TodoItem]]
    def getById(id: TodoId): URIO[R, Option[TodoItem]]
    def delete(id: TodoId): URIO[R, Unit]
    def deleteAll: URIO[R, Unit]

    def create(
      title: String,
      order: Option[Int]
    ): URIO[R, TodoItem]

    def update(
      id: TodoId,
      title: Option[String],
      completed: Option[Boolean],
      order: Option[Int]
    ): ZIO[R, Nothing, Option[TodoItem]]

    def getAllStreamed: ZStream[R, Throwable, TodoItem]
  }

}
