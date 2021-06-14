package com.schuwalow.zio.todo.graphql

import caliban.client.FieldBuilder._
import caliban.client.SelectionBuilder._
import caliban.client._
import caliban.client.Operations._

object Client {

  type TodoItem

  object TodoItem {
    def id: SelectionBuilder[TodoItem, Long] = Field("id", Scalar())

    def item[A](
      innerSelection: SelectionBuilder[TodoPayload, A]
    ): SelectionBuilder[TodoItem, A] = Field("item", Obj(innerSelection))
  }

  type TodoPayload

  object TodoPayload {
    def title: SelectionBuilder[TodoPayload, String] = Field("title", Scalar())

    def completed: SelectionBuilder[TodoPayload, Boolean] =
      Field("completed", Scalar())

    def order: SelectionBuilder[TodoPayload, Option[Int]] =
      Field("order", OptionOf(Scalar()))
  }

  type Queries = RootQuery

  object Queries {

    def allTodoItems[A](
      innerSelection: SelectionBuilder[TodoItem, A]
    ): SelectionBuilder[RootQuery, List[A]] =
      Field("allTodoItems", ListOf(Obj(innerSelection)))

    def todoItem[A](
      id: Long
    )(
      innerSelection: SelectionBuilder[TodoItem, A]
    ): SelectionBuilder[RootQuery, Option[A]] =
      Field(
        "todoItem",
        OptionOf(Obj(innerSelection)),
        arguments = List(Argument("id", id, "Long"))
      )
  }

  type Mutations = RootMutation

  object Mutations {

    def createTodoItem[A](
      title: String,
      order: Option[Int]
    )(
      innerSelection: SelectionBuilder[TodoItem, A]
    ): SelectionBuilder[RootMutation, A] =
      Field(
        "createTodoItem",
        Obj(innerSelection),
        arguments = List(
          Argument("title", title, "String"),
          Argument("order", order, "Option[Int]")
        )
      )

    def deleteTodoItem(id: Long): SelectionBuilder[RootMutation, Unit] =
      Field(
        "deleteTodoItem",
        Scalar(),
        arguments = List(Argument("id", id, "Long"))
      )

    def deleteAllTodoItems: SelectionBuilder[RootMutation, Unit] =
      Field("deleteAllTodoItems", Scalar())

    def updateTodoItem[A](
      id: Long,
      title: Option[String],
      completed: Option[Boolean],
      order: Option[Int]
    )(
      innerSelection: SelectionBuilder[TodoItem, A]
    ): SelectionBuilder[RootMutation, Option[A]] =
      Field(
        "updateTodoItem",
        OptionOf(Obj(innerSelection)),
        arguments = List(
          Argument("id", id, "Long"),
          Argument("title", title, "String"),
          Argument("completed", completed, "Option[Boolean]"),
          Argument("order", order, "Option[Int]")
        )
      )
  }

}
