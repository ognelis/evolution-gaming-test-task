package com.evolutiongaming.maqexoju.components.storage

import com.evolutiongaming.maqexoju.domain.model.User

trait UserStorage[F[_]] {
  def getByNameAndPassword(name: User.Name, password: User.Password): F[Option[User]]
}
