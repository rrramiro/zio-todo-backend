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

    def update(form: TodoItemPatchForm): TodoItem = copy(
      item = item.copy(
        title = form.title.getOrElse(item.title),
        completed = form.completed.getOrElse(item.completed),
        order = form.order.orElse(item.order)
      )
    )
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
    order: Option[Int] = None) {

    def asTodoItem(id: TodoId = TodoId(-1)): TodoItem =
      TodoItem(id, this.asTodoPayload)

    def asTodoPayload: TodoPayload = TodoPayload(title, false, order)
  }

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

}
