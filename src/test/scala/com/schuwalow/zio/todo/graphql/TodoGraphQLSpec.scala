package com.schuwalow.zio.todo.graphql

import caliban.Http4sAdapter
import cats.effect.Blocker
import zio._
import zio.test._
import zio.test.Assertion.equalTo
import zio.interop.catz._
import com.schuwalow.zio.todo.logger._
import com.schuwalow.zio.todo.repository._
import com.schuwalow.zio.todo.graphql.{ Client => GraphQLClient }
import sttp.client3._
import sttp.client3.http4s.Http4sBackend
import sttp.capabilities.fs2.Fs2Streams
import org.http4s.client.{ Client => Http4sClient }
import org.http4s.server.Router
import org.http4s.syntax.kleisli._
import TodoGraphQLSpecUtils._
import caliban.client.Operations._
import caliban.client.SelectionBuilder

object TodoGraphQLSpec extends DefaultRunnableSpec {

  def spec = suite("TodoGraphQL")(
    testM("GQL should list all todo items") {
      withEnv { backend =>
        assertM(
          GraphQLClient.Queries
            .allTodoItems(GraphQLClient.TodoItem.id)
            .call(backend)
        )(equalTo(List.empty[Long]))
      }
    }
  )
}

object TodoGraphQLSpecUtils {
  type AppEnv      = ZEnv with Logger with Repository
  type TodoTask[A] = RIO[AppEnv, A]

  type TodoSttpBackend =
    SttpBackend[TodoTask, Fs2Streams[TodoTask]]

  val baseUrl = uri"http://localhost:8080/api/graphql"

  val graphqlapi = new GraphQLAPI[AppEnv]

  val testingBackend: TodoTask[TodoSttpBackend] = ZIO.runtime[AppEnv] >>= {
    implicit rts =>
      graphqlapi.api.interpreter.map { interpreter =>
        Http4sBackend.usingClient(
          client = Http4sClient.fromHttpApp(
            Router[TodoTask](
              "/api/graphql" -> Http4sAdapter.makeHttpService(interpreter)
            ).orNotFound
          ),
          blocker = Blocker.liftExecutionContext(rts.platform.executor.asEC)
        )
      }
  }

  def withEnv[A](task: TodoSttpBackend => TodoTask[A]): RIO[ZEnv, A] =
    (testingBackend >>= task)
      .provideLayer(
        ZEnv.live ++
          NoLogger.withNoLogger ++
          InMemoryTodoRepository.withInMemoryRepository
      )

  implicit class SelectionBuilderWrapper[Q: IsOperation, A](
    builder: SelectionBuilder[Q, A]) {

    def call(backend: TodoSttpBackend): TodoTask[A] =
      builder.toRequest(baseUrl).send(backend).map(_.body).absolve
  }

}
