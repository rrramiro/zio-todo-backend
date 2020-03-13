package com.schuwalow.zio.todo.repository

import com.schuwalow.zio.todo.domain._
import zio._
import zio.stream.ZStream

object InMemoryTodoRepository {

  val withInMemoryRepository
    : ZLayer[Any, Nothing, Has[Repository.Service[Any]]] = {
    for {
      ref     <- Ref.make(Map.empty[TodoId, TodoItem])
      counter <- Ref.make(0L)
    } yield new InMemoryTodoRepository.Service[Any](ref, counter)
  }.toLayer

  class Service[R](
    ref: Ref[Map[TodoId, TodoItem]],
    counter: Ref[Long])
      extends Repository.Service[R] {

    override def getAll: URIO[R, List[TodoItem]] =
      ref.get.map(_.values.toList)

    override def getById(id: TodoId): URIO[R, Option[TodoItem]] =
      ref.get.map(_.get(id))

    override def delete(id: TodoId): URIO[R, Unit] =
      ref.update(store => store - id).unit

    override def deleteAll: URIO[R, Unit] =
      ref.update(_.empty).unit

    override def create(
      title: String,
      order: Option[Int]
    ): URIO[R, TodoItem] =
      for {
        _     <- counter.update(_ + 1)
        newId <- counter.get.map(TodoId)
        todo  = TodoItem.createItem(title, order, newId)
        _     <- ref.update(store => store + (newId -> todo))
      } yield todo

    override def update(
      id: TodoId,
      title: Option[String],
      completed: Option[Boolean],
      order: Option[Int]
    ): URIO[R, Option[TodoItem]] =
      for {
        oldValue <- getById(id)
        result <- oldValue.fold[UIO[Option[TodoItem]]](ZIO.succeed(None)) { x =>
                   val newValue = x.update(title, completed, order)
                   ref.update(store => store + (newValue.id -> newValue)) *>
                     ZIO.succeed(Some(newValue))
                 }
      } yield result

    override def getAllStreamed: ZStream[R, Nothing, TodoItem] = ZStream.empty
  }

}
