package com.evolutiongaming.maqexoju.domain

import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.refineV
import eu.timepit.refined.types.string.NonEmptyString
import supertagged.TaggedType

object model {

  case class User(
    name: User.Name,
    password: User.Password,
    role: User.Role
  )

  object User {

    object Name extends TaggedType[NonEmptyString] {
      def from(string: String): Either[String, Name] = refineV[NonEmpty](string).map(Name @@ _)
      def unsafeFrom(s: String): Name = from(s).fold(e => throw new IllegalArgumentException(e), identity)
    }
    type Name = Name.Type

    object Password extends TaggedType[NonEmptyString] {
      def from(string: String): Either[String, Password] = refineV[NonEmpty](string).map(Password @@ _)
      def unsafeFrom(s: String): Password = from(s).fold(e => throw new IllegalArgumentException(e), identity)
    }
    type Password = Password.Type

    sealed trait Role
    object Role {
      case object Admin extends Role
      case object Common extends Role
    }

  }

  case class LobbyTable(
    id: LobbyTable.Id,
    name: String,
    participants: Int,
  )
  object LobbyTable {
    object Id extends TaggedType[Long]
    type Id = Id.Type

    val start: Id = LobbyTable.Id(-1L)
  }

  case class LobbyTables(
    tables: Seq[LobbyTable],
  )

}
