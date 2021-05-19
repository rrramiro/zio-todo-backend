package com.schuwalow.zio.todo.http

import cats.syntax.show._
import cats.instances.long._
import com.schuwalow.zio.todo.domain._
import com.schuwalow.zio.todo.logger._
import com.schuwalow.zio.todo.repository._
import io.circe.{ Decoder, Encoder }
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import zio.RIO
import zio.interop.catz._

class TodoRoutes[R <: Logger with Repository](rootUri: String)
    extends Http4sDsl[RIO[R, *]] {

  private def todoItemWithUri(todoItem: TodoItem) = TodoItemWithUri(
    todoItem.id.value,
    s"$rootUri/${todoItem.id.value.show}",
    todoItem.item.title,
    todoItem.item.completed,
    todoItem.item.order
  )

  implicit def circeJsonDecoder[A: Decoder]: EntityDecoder[RIO[R, *], A] =
    jsonOf[RIO[R, *], A]

  implicit def circeJsonEncoder[A: Encoder]: EntityEncoder[RIO[R, *], A] =
    jsonEncoderOf[RIO[R, *], A]

  val routes: HttpRoutes[RIO[R, *]] = HttpRoutes.of[RIO[R, *]] {
    case GET -> Root / LongVar(id) =>
      getById(TodoId(id)) >>= (_.fold(NotFound())(
        x => Ok(todoItemWithUri(x))
      ))
    case GET -> Root =>
      info("getAll") *>
        Ok(getAll.map(_.map(todoItemWithUri)))
    case req @ POST -> Root =>
      req.decode[TodoItemPostForm] { todoItemForm =>
        create(todoItemForm.title, todoItemForm.order)
          .map(todoItemWithUri)
          .flatMap(Created(_))
      }
    case DELETE -> Root / LongVar(id) =>
      getById(TodoId(id)) >>= (_.map(x => delete(x.id))
        .fold(NotFound())(_.flatMap(Ok(_))))
    case DELETE -> Root => deleteAll *> Ok()
    case req @ PATCH -> Root / LongVar(id) =>
      req.decode[TodoItemPatchForm] { updateForm =>
        update(
          TodoId(id),
          updateForm.title,
          updateForm.completed,
          updateForm.order
        ) >>= (_.fold(NotFound())(
          x => Ok(todoItemWithUri(x))
        ))
      }
  }
}
