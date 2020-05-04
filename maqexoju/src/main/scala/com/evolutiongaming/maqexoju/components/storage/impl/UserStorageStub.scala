package com.evolutiongaming.maqexoju.components.storage.impl

import cats.Applicative
import com.evolutiongaming.maqexoju.components.storage.UserStorage
import com.evolutiongaming.maqexoju.domain.model.User

class UserStorageStub[F[_]: Applicative] extends UserStorage[F] {

  override def getByNameAndPassword(
    name: User.Name,
    password: User.Password,
  ): F[Option[User]] = {
    val expectedAdminName = User.Name.unsafeFrom("admin")
    val expectedAdminPassword = User.Password.unsafeFrom("admin")

    val expectedCommonName = User.Name.unsafeFrom("user")
    val expectedCommonPassword = User.Password.unsafeFrom("user")

    val userOpt = (name, password) match {
      case (name, password) if name == expectedAdminName && password == expectedAdminPassword =>
        Some(User(name = name, password = password, role = User.Role.Admin))
      case (name, password) if name == expectedCommonName && password == expectedCommonPassword =>
        Some(User(name = name, password = password, role = User.Role.Common))
      case _ => None
    }

    Applicative[F].pure(userOpt)
  }
}
