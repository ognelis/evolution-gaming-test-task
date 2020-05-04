package com.evolutiongaming.maqexoju.domain

import com.evolutiongaming.maqexoju.domain.model.{LobbyTable, LobbyTables}
import io.circe.{Decoder, Encoder}
import io.circe.derivation.renaming
import io.circe.derivation._

object DomainJsonCodecs {

  implicit val lobbyTableIdEncoder: Encoder[LobbyTable.Id] = Encoder[Long].contramap(identity)
  implicit val lobbyTableIdDecoder: Decoder[LobbyTable.Id] = Decoder[Long].map(LobbyTable.Id(_))

  implicit val lobbyTableEncoder: Encoder[LobbyTable] = deriveEncoder(renaming.snakeCase, None)
  implicit val lobbyTableDecoder: Decoder[LobbyTable] = deriveDecoder(renaming.snakeCase, true, None)

  implicit val lobbyTablesEncoder: Encoder[LobbyTables] = deriveEncoder(renaming.snakeCase, None)
  implicit val lobbyTablesDecoder: Decoder[LobbyTables] = deriveDecoder(renaming.snakeCase, true, None)

}
