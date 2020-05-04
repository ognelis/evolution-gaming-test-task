package com.evolutiongaming.maqexoju.components.server

import cats.effect.Sync
import com.evolutiongaming.maqexoju.components.auth.AuthService
import com.evolutiongaming.maqexoju.components.storage.LobbyTableStorage
import com.evolutiongaming.maqexoju.domain.model.{LobbyTable, LobbyTables, User}
import io.circe.Decoder.Result
import io.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{AuthedRoutes, EntityEncoder, HttpRoutes}

class HttpLobbyApi[F[_]: Sync](
  authService: AuthService[F],
  lobbyTableStorage: LobbyTableStorage[F],
) extends Http4sDsl[F] {

  import io.circe.syntax._
  import org.http4s.circe.jsonEncoderWithPrinter

  private implicit val jsonEncoder: EntityEncoder[F, Json] = jsonEncoderWithPrinter(Printer.spaces2)

  def routes: HttpRoutes[F] = {
    import HttpLobbyApi._
    import cats.syntax.semigroupk._
    import cats.syntax.flatMap._
    import cats.syntax.functor._
    import org.http4s.circe.CirceEntityDecoder._

    val authenticatedRoutes = authService.authenticateMiddleware(AuthedRoutes.of[User, F] {
      case GET -> Root as _ =>
        Ok(lobbyTableStorage.all.map(_.asJson))
    })

    val authorizedRoutes = authService.authorizeMiddleware(
      Set(User.Role.Admin)
    )(AuthedRoutes.of[User, F] {
      case authedRequest@POST -> Root as _ =>
        import com.evolutiongaming.maqexoju.domain.DomainJsonCodecs.lobbyTableEncoder

        authedRequest.req.as[LobbyTableApiRequest[LobbyTableCreate]].flatMap {
          case parsedRequest if parsedRequest.command == ApiCommand.AddTable =>
            Created(lobbyTableStorage.create(
              parsedRequest.body.lobbyTable,
              parsedRequest.body.afterId
            ).map(_.asJson))
          case _ =>
            BadRequest(Json.obj())
        }
      case authedRequest@PUT -> Root as _ =>
        import com.evolutiongaming.maqexoju.domain.DomainJsonCodecs.{lobbyTableDecoder, lobbyTableEncoder}

        authedRequest.req.as[LobbyTableApiRequest[LobbyTable]].flatMap {
          case parsedRequest if parsedRequest.command == ApiCommand.UpdateTable =>
            lobbyTableStorage.update(
              parsedRequest.body,
            ).flatMap {
              case None => BadRequest(Json.obj())
              case Some(table) => Ok(table.asJson)
            }
          case _ =>
            BadRequest(Json.obj())
        }
      case authedRequest@DELETE -> Root as _ =>
        authedRequest.req.as[LobbyTableApiRequest[LobbyTableDelete]].flatMap {
          case parsedRequest if parsedRequest.command == ApiCommand.RemoveTable =>
            Ok(lobbyTableStorage.delete(parsedRequest.body.id).map(_.asJson))
          case _ =>
            BadRequest(Json.obj())
        }
    })

    authenticatedRoutes <+> authorizedRoutes
  }
}

object HttpLobbyApi {

  import io.circe.derivation.{renaming, _}

  sealed trait ApiCommand
  object ApiCommand {
    case object AddTable extends ApiCommand
    case object UpdateTable extends ApiCommand
    case object RemoveTable extends ApiCommand
  }

  final case class LobbyTableApiRequest[A](
    command: ApiCommand,
    body: A,
  )

  implicit val apiCommandDecoder: Decoder[ApiCommand] = {
    Decoder[String].emap {
      case "add_table" => Right(ApiCommand.AddTable)
      case "update_table" => Right(ApiCommand.UpdateTable)
      case "remove_table" => Right(ApiCommand.RemoveTable)
      case unknown => Left(unknown)
    }
  }

  implicit def lobbyTableApiRequestDecoder[A: Decoder]: Decoder[LobbyTableApiRequest[A]] = {
    new Decoder[LobbyTableApiRequest[A]] {
      override def apply(c: HCursor): Result[LobbyTableApiRequest[A]] = {
        for {
          commandType <- c.downField("$type").as[ApiCommand]
          lobbyTableCreate <- Decoder[A].apply(c)
        } yield LobbyTableApiRequest(commandType, lobbyTableCreate)
      }
    }
  }

  implicit val httpLobbyListResponseEncoder: Encoder[LobbyTables] = {
    import com.evolutiongaming.maqexoju.domain.DomainJsonCodecs.lobbyTablesEncoder
    lobbyTablesEncoder.mapJson(json =>
      json.deepMerge(Json.obj("$type" -> Json.fromString("table_list")))
    )
  }

  final case class LobbyTableCreate(
    lobbyTable: LobbyTable,
    afterId: LobbyTable.Id,
  )

  implicit val lobbyTableCreateDecoder: Decoder[LobbyTableCreate] = {
    import com.evolutiongaming.maqexoju.domain.DomainJsonCodecs._
    deriveDecoder(renaming.snakeCase, true, None)
  }

  final case class LobbyTableDelete(
    id: LobbyTable.Id
  )

  implicit val lobbyTableDeleteDecoder: Decoder[LobbyTableDelete] = {
    import com.evolutiongaming.maqexoju.domain.DomainJsonCodecs._
    deriveDecoder(renaming.snakeCase, true, None)
  }

}
