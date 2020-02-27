package com.schuwalow.zio.todo.graphql

import caliban.Http4sAdapter
import zio._
import zio.test._
import zio.test.Assertion.equalTo
import zio.interop.catz._
import zio.macros.delegate.syntax._
import com.schuwalow.zio.todo.logger.{ Logger, NoLogger }
import com.schuwalow.zio.todo.repository.{ InMemoryTodoRepository, Repository }
import com.schuwalow.zio.todo.graphql.{ Client => GraphQLClient }
import sttp.client._
import sttp.client.http4s.Http4sBackend
import org.http4s.client.{ Client => Http4sClient }
import org.http4s.server.Router
import org.http4s.syntax.kleisli._
import TodoGraphQLSpecUtils._
import caliban.client.Operations._
import caliban.client.SelectionBuilder

object TodoGraphQLSpec
    extends DefaultRunnableSpec(
      suite("TodoGraphQL")(
        testM("GQL should list all todo items") {
          withEnv { implicit backend =>
            assertM(
              GraphQLClient.Queries
                .allTodoItems(GraphQLClient.TodoItem.id)
                .call,
              equalTo(List.empty[Long])
            )
          }
        }
      )
    )

object TodoGraphQLSpecUtils {
  type AppEnv      = ZEnv with Logger with Repository
  type TodoTask[A] = RIO[AppEnv, A]

  type TodoSttpBackend =
    SttpBackend[TodoTask, fs2.Stream[TodoTask, Byte], NothingT]

  val baseUrl = uri"http://localhost:8080/api/graphql"

  val graphqlapi = new GraphQLAPI[AppEnv]

  val testingBackend: TodoTask[TodoSttpBackend] = ZIO.runtime[AppEnv] >>= {
    implicit rts =>
      graphqlapi.api.interpreter.map { interpreter =>
        Http4sBackend.usingClient(
          Http4sClient.fromHttpApp(
            Router[TodoTask](
              "/api/graphql" -> Http4sAdapter.makeHttpService(interpreter)
            ).orNotFound
          )
        )
      }
  }

  def withEnv[A](task: TodoSttpBackend => TodoTask[A]): RIO[ZEnv, A] =
    ZIO.environment[ZEnv] @@
      NoLogger.withNoLogger @@
      InMemoryTodoRepository.withInMemoryRepository >>> (testingBackend >>= task)

  implicit class SelectionBuilderWrapper[Q: IsOperation, A](
    builder: SelectionBuilder[Q, A]
  )(implicit
    backend: TodoSttpBackend) {

    def call: TodoTask[A] =
      builder.toRequest(baseUrl).send().map(_.body).absolve
  }

}
