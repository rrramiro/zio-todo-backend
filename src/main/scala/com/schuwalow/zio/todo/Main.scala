package com.schuwalow.zio.todo

import caliban.Http4sAdapter
import cats.effect._
import com.schuwalow.zio.todo.config._
import com.schuwalow.zio.todo.graphql.GraphQLAPI
import com.schuwalow.zio.todo.http.TodoRoutes
import com.schuwalow.zio.todo.logger._
import com.schuwalow.zio.todo.repository._
import fs2.Stream.Compiler._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.server.Router
import zio._
import zio.console._
import zio.interop.catz._
import zio.macros.delegate.syntax._
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException

object Main extends ManagedApp {

  override def run(args: List[String]): ZManaged[ZEnv, Nothing, Int] =
    (for {
      cfg <- ZIO
              .fromEither(ConfigSource.default.load[Config])
              .mapError(ConfigReaderException(_))
              .toManaged_
      _ <- ZIO.environment[ZEnv] @@
            Slf4jLogger.withSlf4jLogger("zio-todo-backend") @@
            DoobieTodoRepository.withDoobieTodoRepository(cfg.dbConfig) >>>
            runHttp(cfg).toManaged_
    } yield ())
      .foldM(
        err =>
          putStrLn(s"Execution failed with: ${err.getMessage}")
            .as(1)
            .toManaged_,
        _ => ZManaged.succeed(0)
      )

  def runHttp[R <: ZEnv with Logger with Repository](
    cfg: Config
  ): RIO[R, Unit] = ZIO.runtime[R] >>= { implicit rts =>
    new GraphQLAPI[R].api.interpreter >>= { interpreter =>
      BlazeServerBuilder[RIO[R, *]]
        .bindHttp(cfg.appConfig.port, "0.0.0.0")
        .withHttpApp(CORS {
          Router[RIO[R, *]](
            "/todos" -> new TodoRoutes(
              s"${cfg.appConfig.baseUrl}/todos"
            ).routes,
            "/api/graphql" -> Http4sAdapter.makeHttpService(interpreter)
          ).orNotFound
        })
        .serve
        .compile[RIO[R, *], RIO[R, *], ExitCode]
        .drain
    }
  }

}
