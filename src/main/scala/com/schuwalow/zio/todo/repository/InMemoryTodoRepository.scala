package com.schuwalow.zio.todo.repository

import com.schuwalow.zio.todo.domain._
import zio._
import zio.macros.delegate._

final class InMemoryTodoRepository(
  ref: Ref[Map[TodoId, TodoItem]],
  counter: Ref[Long])
    extends Repository {

  val todoRepository = new InMemoryTodoRepository.Service[Any](ref, counter)
}

object InMemoryTodoRepository {

  val withInMemoryRepository = enrichWithM[Repository] {
    for {
      ref     <- Ref.make(Map.empty[TodoId, TodoItem])
      counter <- Ref.make(0L)
    } yield new InMemoryTodoRepository(ref, counter)
  }

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

    override def create(todoItemForm: TodoItemPostForm): URIO[R, TodoItem] =
      for {
        newId <- counter.update(_ + 1).map(TodoId)
        todo  = todoItemForm.asTodoItem(newId)
        _     <- ref.update(store => store + (newId -> todo))
      } yield todo

    override def update(
      id: TodoId,
      todoItemForm: TodoItemPatchForm
    ): URIO[R, Option[TodoItem]] =
      for {
        oldValue <- getById(id)
        result <- oldValue.fold[UIO[Option[TodoItem]]](ZIO.succeed(None)) { x =>
                   val newValue = x.update(todoItemForm)
                   ref.update(store => store + (newValue.id -> newValue)) *>
                     ZIO.succeed(Some(newValue))
                 }
      } yield result
  }

}
