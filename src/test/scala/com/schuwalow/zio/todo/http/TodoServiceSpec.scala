package com.schuwalow.zio.todo.http

import HTTPSpec._
import TodoServiceSpecUtils._
import com.schuwalow.zio.todo.domain._
import com.schuwalow.zio.todo.logger._
import com.schuwalow.zio.todo.repository._
import cats.syntax.show._
import cats.instances.long._
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.server.Router
import zio._
import zio.test._
import zio.interop.catz.taskConcurrentInstance

object TodoServiceSpec extends DefaultRunnableSpec {

  def spec = suite("TodoService")(
    testM("should create new todo items") {
      withEnv {
        checkRequestJson(app.run(setupReq), Status.Created, record1)
      }
    },
    testM("should list all todo items") {
      withEnv {
        checkRequestJson(
          app.run(setupReq) *>
            app.run(setupReq) *>
            app.run(getReq),
          Status.Ok,
          json"""[$record1,$record2]"""
        )
      }
    },
    testM("should delete todo items by id") {
      withEnv {
        checkRequestJson(
          (app.run(setupReq) >>=
            (_.as[TodoItemWithUri].map(_.id)) >>=
            (id => app.run(deleteWithIdReq(id)))) *> app.run(getReq),
          Status.Ok,
          json"""[]"""
        )
      }
    },
    testM("should delete all todo items") {
      withEnv {
        checkRequestJson(
          app.run(setupReq) *>
            app.run(setupReq) *>
            app.run(deleteReq) *>
            app.run(getReq),
          Status.Ok,
          json"""[]"""
        )
      }
    },
    testM("should update todo items") {
      withEnv {
        checkRequestJson(
          (app
            .run(setupReq) >>=
            (_.as[TodoItemWithUri].map(_.id)) >>=
            (id => app.run(updateReq(id)))) *> app.run(getReq),
          Status.Ok,
          json"""[$record1Updated]"""
        )
      }
    }
  )
}

object TodoServiceSpecUtils {
  type AppEnv      = ZEnv with Logger with Repository
  type TodoTask[A] = RIO[AppEnv, A]

  implicit val todoItemWithUriJsonDecoder
    : EntityDecoder[TodoTask, TodoItemWithUri] =
    jsonOf[TodoTask, TodoItemWithUri]

  val todoRoutes = new TodoRoutes[AppEnv]("")

  val app: HttpApp[TodoTask] = Router[TodoTask](
    "/" -> todoRoutes.routes
  ).orNotFound

  def withEnv[A](task: TodoTask[A]): RIO[ZEnv, A] = task.provideLayer(
    ZEnv.live ++
      NoLogger.withNoLogger ++
      InMemoryTodoRepository.withInMemoryRepository
  )

  val setupReq: Request[TodoTask] =
    request[TodoTask](Method.POST, "/")
      .withEntity(json"""{"title": "Test"}""")

  val deleteReq: Request[TodoTask] =
    request[TodoTask](Method.DELETE, "/")

  val getReq: Request[TodoTask] =
    request[TodoTask](Method.GET, "/")

  def updateReq(id: Long): Request[TodoTask] =
    request[TodoTask](Method.PATCH, s"/${id.show}")
      .withEntity(json"""{"title": "Test1"}""")

  def deleteWithIdReq(id: Long): Request[TodoTask] =
    request[TodoTask](Method.DELETE, s"/${id.show}")

  val record1 =
    json"""{"id": 1, "url": "/1", "title": "Test", "completed":false, "order":null}"""

  val record1Updated =
    json"""{"id": 1, "url": "/1", "title": "Test1", "completed":false, "order":null}"""

  val record2 =
    json"""{"id": 2, "url": "/2", "title": "Test", "completed":false, "order":null}"""
}
