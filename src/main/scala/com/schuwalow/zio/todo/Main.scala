package com.schuwalow.zio.todo

import caliban.Http4sAdapter
import cats.syntax.apply._
import com.schuwalow.zio.todo.config._
import com.schuwalow.zio.todo.graphql.GraphQLAPI
import com.schuwalow.zio.todo.http.TodoRoutes
import com.schuwalow.zio.todo.logger._
import com.schuwalow.zio.todo.repository._
import fs2.Stream.Compiler._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.middleware.CORS
import org.http4s.server.Router
import zio._
import zio.console._
import zio.interop.catz._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

object Main extends ManagedApp {

  override def run(args: List[String]): ZManaged[ZEnv, Nothing, ExitCode] =
    (for {
      cfg <- ZIO
              .fromEither(ConfigSource.default.load[Config])
              .mapError(ConfigReaderException(_))
              .toManaged_
      _ <- runHttp(cfg)
            .provideLayer(
              ZEnv.live ++
                Slf4jLogger.withSlf4jLogger("zio-todo-backend") ++
                DoobieTodoRepository.withDoobieTodoRepository(cfg.dbConfig)
              //InMemoryTodoRepository.withInMemoryRepository
            )
            .toManaged_
    } yield ())
      .foldM(
        err =>
          putStrLn(s"Execution failed with: ${err.getMessage}")
            .as(ExitCode.failure)
            .toManaged_,
        _ => ZManaged.succeed(ExitCode.success)
      )

  def runHttp[R <: ZEnv with Logger with Repository](
    cfg: Config
  ): RIO[R, Unit] = ZIO.runtime[R] >>= { implicit rts =>
    val graphql = new GraphQLAPI[R]
    (graphql.federated.interpreter, graphql.api.interpreter).tupled >>= {
      case (federatedInterpreter, apiInterpreter) =>
        BlazeServerBuilder[RIO[R, *]](rts.platform.executor.asEC)
          .bindHttp(cfg.appConfig.port, "0.0.0.0")
          .withHttpApp(CORS {
            Router[RIO[R, *]](
              "/todos" -> new TodoRoutes(
                s"${cfg.appConfig.baseUrl}/todos"
              ).routes,
              "/api/graphql" -> Http4sAdapter.makeHttpService(apiInterpreter),
              "/api/federated" -> Http4sAdapter.makeHttpService(
                federatedInterpreter
              )
            ).orNotFound
          })
          .serve
          .compile//[RIO[R, *], RIO[R, *], cats.effect.ExitCode]
          .drain
    }
  }

}
