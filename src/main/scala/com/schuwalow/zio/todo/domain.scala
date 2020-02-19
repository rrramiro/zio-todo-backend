package com.schuwalow.zio.todo

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._
import io.getquill.Embedded

object domain {

  final case class TodoId(value: Long) extends AnyVal

  final case class TodoPayload(
    title: String,
    completed: Boolean,
    order: Option[Int])
      extends Embedded

  final case class TodoItem(
    id: TodoId,
    item: TodoPayload) {

    def update(
      title: Option[String],
      completed: Option[Boolean],
      order: Option[Int]
    ): TodoItem = copy(
      item = item.copy(
        title = title.getOrElse(item.title),
        completed = completed.getOrElse(item.completed),
        order = order.orElse(item.order)
      )
    )
  }

  object TodoItem {

    def createItem(
      title: String,
      order: Option[Int] = None,
      id: TodoId = TodoId(-1)
    ): TodoItem =
      TodoItem(id, TodoPayload(title, false, order))
  }

  final case class TodoItemWithUri(
    id: Long,
    url: String,
    title: String,
    completed: Boolean,
    order: Option[Int])

  object TodoItemWithUri {
    implicit val encoder: Encoder[TodoItemWithUri] = deriveEncoder
    implicit val decoder: Decoder[TodoItemWithUri] = deriveDecoder
  }

  final case class TodoItemPostForm(
    title: String,
    order: Option[Int] = None)

  object TodoItemPostForm {
    implicit val decoder: Decoder[TodoItemPostForm] = deriveDecoder
  }

  final case class TodoItemPatchForm(
    title: Option[String] = None,
    completed: Option[Boolean] = None,
    order: Option[Int] = None)

  object TodoItemPatchForm {
    implicit val decoder: Decoder[TodoItemPatchForm] = deriveDecoder
  }

  final case class TodoItemCreateArgs(
    title: String,
    order: Option[Int] = None)

  final case class TodoItemUpdateArgs(
    id: TodoId,
    title: Option[String] = None,
    completed: Option[Boolean] = None,
    order: Option[Int] = None)
  final case class TodoItemArgs(id: TodoId)

}
