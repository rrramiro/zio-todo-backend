package com.schuwalow.zio.todo.repository

import com.schuwalow.zio.todo.domain._
import com.schuwalow.zio.todo.config._
import cats.effect.Blocker
import cats.implicits._
import io.getquill.{ idiom => _, _ }
import doobie._
import doobie.implicits._
import doobie.free.connection
import doobie.hikari._
import doobie.quill.DoobieContext
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import zio._
import zio.blocking.Blocking
import zio.interop.catz._
import zio.stream.interop.fs2z._
import zio.stream.ZStream

object DoobieTodoRepository {

  def withDoobieTodoRepository(
    cfg: DBConfig
  ): ZLayer[Blocking, Throwable, Has[Repository.Service[Any]]] = {
    Task {
      Flyway
        .configure()
        .dataSource(cfg.url, cfg.user, cfg.password)
        .load()
        .migrate()
    }.unit.toManaged_ *> (ZIO.runtime[Blocking].toManaged_ >>= { implicit rt =>
      HikariTransactor
        .newHikariTransactor[Task](
          cfg.driver,
          cfg.url,
          cfg.user,
          cfg.password,
          rt.platform.executor.asEC,
          Blocker.liftExecutionContext(rt.environment.get.blockingExecutor.asEC)
        )
        .toManaged
    }).map(transactor => new DoobieTodoRepository.Service[Any](transactor))
  }.toLayer

  object SqlContext extends DoobieContext.H2(UpperCase) {
    implicit val todosInsertMeta = insertMeta[TodoItem](_.id)
    implicit val todosUpdateMeta = updateMeta[TodoItem](_.id)

    val todosTable = quote {
      querySchema[TodoItem]("TODOS", _.item.order -> "ORDERING")
    }

    @SuppressWarnings(Array("org.wartremover.warts.StringPlusAny"))
    def create(todoItem: TodoItem): ConnectionIO[TodoId] =
      run(quote {
        todosTable
          .insert(
            _.item.title     -> lift(todoItem.item.title),
            _.item.completed -> lift(todoItem.item.completed),
            _.item.order     -> lift(todoItem.item.order)
          )
          .returningGenerated(_.id)
      })

    def get(id: TodoId): ConnectionIO[Option[TodoItem]] =
      run(quote {
        todosTable.filter(_.id == lift(id))
      }).map(_.headOption)

    val getAll: ConnectionIO[List[TodoItem]] = run(todosTable)

    def delete(id: TodoId): ConnectionIO[Long] =
      run(quote {
        todosTable.filter(_.id == lift(id)).delete
      })

    val deleteAll: ConnectionIO[Long] = run(quote {
      todosTable.delete
    })

    val getAllStreamed: fs2.Stream[ConnectionIO, TodoItem] =
      stream[TodoItem](quote {
        todosTable
      })

    def update(todoItem: TodoItem): ConnectionIO[Long] =
      run(quote {
        todosTable
          .filter(_.id == lift(todoItem.id))
          .update(lift(todoItem))
      })
  }

  class Service[R](xa: Transactor[Task]) extends Repository.Service[R] {

    override def getAll: URIO[R, List[TodoItem]] =
      SqlContext.getAll
        .transact(xa)
        .orDie

    override def getById(id: TodoId): URIO[R, Option[TodoItem]] =
      SqlContext
        .get(id)
        .transact(xa)
        .orDie

    override def delete(id: TodoId): URIO[R, Unit] =
      SqlContext
        .delete(id)
        .transact(xa)
        .unit
        .orDie

    override def deleteAll: URIO[R, Unit] =
      SqlContext.deleteAll
        .transact(xa)
        .unit
        .orDie

    override def getAllStreamed: ZStream[R, Throwable, TodoItem] =
      SqlContext.getAllStreamed
        .transact(xa)
        .toZStream()

    override def create(
      title: String,
      order: Option[Int]
    ): URIO[R, TodoItem] =
      SqlContext
        .create(TodoItem.createItem(title))
        .map(id => TodoItem.createItem(title, id = id))
        .transact(xa)
        .orDie

    override def update(
      id: TodoId,
      title: Option[String],
      completed: Option[Boolean],
      order: Option[Int]
    ): URIO[R, Option[TodoItem]] =
      (for {
        oldItem <- SqlContext.get(id)
        newItem = oldItem.map(_.update(title, completed, order))
        _       <- newItem.fold(connection.unit)(item => SqlContext.update(item).void)
      } yield newItem)
        .transact(xa)
        .orDie
  }
}
