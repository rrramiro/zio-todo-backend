package com.schuwalow.zio.todo

import com.schuwalow.zio.todo.domain._
import zio._
import zio.stream.ZStream

package object repository
    extends Repository.Service[Has[Repository.Service[Any]]] {
  type Repository = Has[Repository.Service[Any]]

  def create(
    title: String,
    order: Option[Int]
  ): URIO[Repository, TodoItem] =
    ZIO.accessM(_.get.create(title, order))

  def getById(id: TodoId): URIO[Repository, Option[TodoItem]] =
    ZIO.accessM(_.get.getById(id))

  def getAll: URIO[Repository, List[TodoItem]] =
    ZIO.accessM(_.get.getAll)

  def delete(id: TodoId): URIO[Repository, Unit] =
    ZIO.accessM(_.get.delete(id))

  def deleteAll: URIO[Repository, Unit] =
    ZIO.accessM(_.get.deleteAll)

  def update(
    id: TodoId,
    title: Option[String],
    completed: Option[Boolean],
    order: Option[Int]
  ): URIO[Repository, Option[TodoItem]] =
    ZIO.accessM(_.get.update(id, title, completed, order))

  def getAllStreamed: ZStream[Repository, Nothing, TodoItem] =
    ZStream.accessStream(_.get.getAllStreamed)

}
