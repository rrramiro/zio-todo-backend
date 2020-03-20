package com.schuwalow.zio.todo.graphql

import caliban.GraphQL.graphQL
import caliban._
import caliban.Value.IntValue
import caliban.introspection.adt._
import caliban.schema.Annotations._
import caliban.schema.Step.ObjectStep
import caliban.schema._
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._
import com.schuwalow.zio.todo.domain._
import com.schuwalow.zio.todo.repository._
import zio._
import zio.duration._
//import zio.stream.ZStream

import scala.language.postfixOps

class GraphQLAPI[R <: Repository with console.Console with clock.Clock]
    extends GenericSchema[R] {

  implicit val todoIdSchema: Schema[Any, TodoId] = new Schema[Any, TodoId] {
    override def toType(isInput: Boolean): __Type = Types.long

    override def resolve(value: TodoId): Step[Any] =
      PureStep(IntValue(value.value))
  }

  implicit val _anySchema = new Schema[Any, _Any] {
    override def toType(isInput: Boolean): __Type = Types.makeScalar("_Any")
    override def resolve(value: _Any): Step[Any] = ObjectStep[Any]("_Any", fields = Map.empty[String, Step[Any]])
  }

  case class Queries(
    _entities: EntitiesArgs => URIO[R, List[_Entity]],
    _service: URIO[R, _Service],
    @GQLDescription("Return all todo items")
    allTodoItems: URIO[R, List[TodoItem]],
    todoItem: TodoItemArgs => URIO[R, Option[TodoItem]])

  case class Mutations(
    createTodoItem: TodoItemCreateArgs => URIO[R, TodoItem],
    deleteTodoItem: TodoItemArgs => URIO[R, Unit],
    deleteAllTodoItems: URIO[R, Unit],
    updateTodoItem: TodoItemUpdateArgs => URIO[R, Option[TodoItem]])

  //case class Subscriptions(characterDeleted: ZStream[R, Nothing, String])

  def _entities(args: EntitiesArgs) = {
    println(args.representations)
    IO.succeed(List.empty[_Entity])
  }

  def _service = IO.succeed(_Service(api.render))

  lazy val api: GraphQL[R] =
    graphQL(
      RootResolver(
        Queries(
          _entities,
          _service,
          getAll,
          args => getById(args.id)
        ),
        Mutations(
          args => create(args.title, args.order),
          args => delete(args.id),
          deleteAll,
          args => update(args.id, args.title, args.completed, args.order)
        )
        //, Subscriptions(deletedEvents)
      )
    ) @@
      maxFields(200) @@               // query analyzer that limit query fields
      maxDepth(30) @@                 // query analyzer that limit query depth
      timeout(3 seconds) @@           // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      apolloTracing                   // wrapper for https://github.com/apollographql/apollo-tracing

}
