package com.schuwalow.zio.todo

import com.schuwalow.zio.todo.domain._
import zio._
//import zio.stream.ZStream

package object repository extends Repository.Service[Repository] { // TodoRepository.Accessors

  override def create(
    title: String,
    order: Option[Int]
  ): URIO[Repository, TodoItem] =
    ZIO.accessM(_.todoRepository.create(title, order))

  override def getById(id: TodoId): URIO[Repository, Option[TodoItem]] =
    ZIO.accessM(_.todoRepository.getById(id))

  override def getAll: URIO[Repository, List[TodoItem]] =
    ZIO.accessM(_.todoRepository.getAll)

  override def delete(id: TodoId): URIO[Repository, Unit] =
    ZIO.accessM(_.todoRepository.delete(id))

  override def deleteAll: URIO[Repository, Unit] =
    ZIO.accessM(_.todoRepository.deleteAll)

  override def update(
    id: TodoId,
    title: Option[String],
    completed: Option[Boolean],
    order: Option[Int]
  ): URIO[Repository, Option[TodoItem]] =
    ZIO.accessM(_.todoRepository.update(id, title, completed, order))

  //override def deletedEvents: ZStream[Repository, Nothing, String] = ZStream.accessM(_.todoRepository.deletedEvents)

}
