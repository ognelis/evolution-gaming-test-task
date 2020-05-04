package com.evolutiongaming.maqexoju.components.auth.impl

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import com.evolutiongaming.maqexoju.components.auth.AuthService
import com.evolutiongaming.maqexoju.components.auth.AuthService.NotAuthorized
import com.evolutiongaming.maqexoju.components.auth.impl.BasicAuthServiceImpl.Config
import com.evolutiongaming.maqexoju.components.storage.UserStorage
import com.evolutiongaming.maqexoju.domain.model.User
import io.circe.Json
import org.http4s._
import org.http4s.headers.`WWW-Authenticate`
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator

class BasicAuthServiceImpl[F[_]: Sync: EntityEncoder[*[*], NotAuthorized.type]] private(
  config: Config,
  userStorage: UserStorage[F],
) extends AuthService[F] {

  private val authenticator: BasicAuthenticator[F, User] = { credentials: BasicCredentials =>
    (for {
      name <- User.Name.from(credentials.username)
      password <- User.Password.from(credentials.password)
    } yield (name, password)) match {
      case Left(_) => Sync[F].pure(None)
      case Right((name, password)) => userStorage.getByNameAndPassword(name, password)
    }
  }

  override val authenticateMiddleware: AuthMiddleware[F, User] = {
    challenged(BasicAuth.challenge(config.realm, authenticator))
  }

  override def authorizeMiddleware(rolesAllowed: Set[User.Role]): AuthMiddleware[F, User] = {
    challenged(BasicAuth.challenge(config.realm, authorizer(rolesAllowed)))
  }

  private def challenged[A](
    challenge: Kleisli[F, Request[F], Either[Challenge, AuthedRequest[F, A]]])(
    routes: AuthedRoutes[A, F]
  ): HttpRoutes[F] = {

    def unauthorized(challenge: Challenge): Response[F] = {
      Response(Status.Unauthorized)
        .putHeaders(`WWW-Authenticate`(challenge))
        .withEntity(NotAuthorized)
    }

    Kleisli { req =>
      OptionT[F, Response[F]] {
        Sync[F].flatMap(challenge(req)) {
          case Left(challenge) => Sync[F].pure(Some(unauthorized(challenge)))
          case Right(authedRequest) => routes(authedRequest).value
        }
      }
    }
  }

  private def authorizer(
    allowedRoles: Set[User.Role],
  ): BasicAuthenticator[F, User] = { credentials: BasicCredentials =>
    import cats.syntax.functor._
    authenticator(credentials).map {
      case Some(user) if allowedRoles(user.role) => Some(user)
      case _ => None
    }
  }
}

object BasicAuthServiceImpl {

  def withJsonResponse[F[_]: Sync](
    config: Config,
    userStorage: UserStorage[F],
  )(implicit jsonEncoder: EntityEncoder[F, Json]): BasicAuthServiceImpl[F] = {
    import AuthService._
    import org.http4s.circe.CirceEntityEncoder._

    new BasicAuthServiceImpl(
      config = config,
      userStorage = userStorage,
    )
  }

  case class Config(
    realm: String,
  )
}
