package com.schuwalow.zio.todo

import com.schuwalow.zio.todo.domain._
import zio._

package object repository extends Repository.Service[Repository] { // TodoRepository.Accessors

  override def create(
    todoItemForm: TodoItemPostForm
  ): URIO[Repository, TodoItem] =
    ZIO.accessM(_.todoRepository.create(todoItemForm))

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
    todoItemForm: TodoItemPatchForm
  ): URIO[Repository, Option[TodoItem]] =
    ZIO.accessM(_.todoRepository.update(id, todoItemForm))

}
