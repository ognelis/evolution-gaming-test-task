package com.evolutiongaming.maqexoju.components.storage

import com.evolutiongaming.maqexoju.domain.model.{LobbyTable, LobbyTables}

trait LobbyTableStorage[F[_]] {
  def all: F[LobbyTables]

  def create(lobbyTable: LobbyTable, afterId: LobbyTable.Id): F[LobbyTable]

  def update(lobbyTable: LobbyTable): F[Option[LobbyTable]]

  def delete(id: LobbyTable.Id): F[Unit]
}
