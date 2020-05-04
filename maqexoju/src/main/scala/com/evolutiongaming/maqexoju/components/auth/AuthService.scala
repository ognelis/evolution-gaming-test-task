package com.evolutiongaming.maqexoju.components.auth

import com.evolutiongaming.maqexoju.domain.model.User
import io.circe.{Encoder, Json}
import org.http4s.server.AuthMiddleware

trait AuthService[F[_]] {
  def authenticateMiddleware: AuthMiddleware[F, User]

  def authorizeMiddleware(rolesAllowed: Set[User.Role]): AuthMiddleware[F, User]
}

object AuthService {

  case object NotAuthorized

  implicit val notAuthorizedEncoder: Encoder[NotAuthorized.type] = {
    new Encoder[NotAuthorized.type] {
      override def apply(a: NotAuthorized.type): Json = Json.obj(
        "$type" -> Json.fromString("not_authorized")
      )
    }
  }

}
