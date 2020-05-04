package com.evolutiongaming.maqexoju.components.server

import cats.effect.{ConcurrentEffect, Resource, Sync, Timer}
import com.evolutiongaming.maqexoju.components.auth.AuthService
import com.evolutiongaming.maqexoju.components.storage.LobbyTableStorage
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server}

class HttpServer[F[_]: Sync] private (
  authService: AuthService[F],
  lobbyTableStorage: LobbyTableStorage[F],
) extends Http4sDsl[F] {

  private val httpLobbyApi = new HttpLobbyApi[F](
    authService = authService,
    lobbyTableStorage = lobbyTableStorage,
  )

  private def routes: HttpRoutes[F] = {
    Router[F](
      "/lobby_api" -> httpLobbyApi.routes,
    )
  }
}

object HttpServer {

  def apply[F[_]: ConcurrentEffect: Timer](
    config: Config,
    authService: AuthService[F],
    lobbyTableStorage: LobbyTableStorage[F],
  ): Resource[F, Server[F]] = {
    import org.http4s.syntax.kleisli._

    val httpServer = new HttpServer[F](
      authService = authService,
      lobbyTableStorage = lobbyTableStorage,
    )

    BlazeServerBuilder[F]
      .bindHttp(
        port = config.port,
        host = config.host,
      )
      .withHttpApp(httpServer.routes.orNotFound)
      .resource
  }

  case class Config(
    host: String,
    port: Int,
  )

}
