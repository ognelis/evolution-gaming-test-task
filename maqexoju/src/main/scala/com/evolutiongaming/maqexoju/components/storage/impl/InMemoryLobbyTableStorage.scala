package com.evolutiongaming.maqexoju.components.storage.impl

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.evolutiongaming.maqexoju.components.storage.LobbyTableStorage
import com.evolutiongaming.maqexoju.domain.model.{LobbyTable, LobbyTables}

class InMemoryLobbyTableStorage[F[_]: Sync](
  cache: Ref[F, Seq[LobbyTable]]
) extends LobbyTableStorage[F] {

  import cats.syntax.all._

  override def all: F[LobbyTables] = cache.get.map(LobbyTables)

  //TODO should deal if it's update.
  //TODO should deal if lobby table with `afterId` doesn't exist.
  //TODO signature of returning value should be F[Either[Throwable, LobbyTable]
  override def create(lobbyTable: LobbyTable, afterId: LobbyTable.Id): F[LobbyTable] = {
    cache.modify { tables =>
      if (tables.isEmpty && afterId != LobbyTable.start) {
        (tables, Left(new IllegalArgumentException(s"Adding new table to tables should be uses with start id=$afterId")))
      } else if (tables.exists(_.id == lobbyTable.id)) {
        (tables, Left(new IllegalArgumentException(s"Adding the same is illegal id=${lobbyTable.id}")))
      } else {
        if (afterId == LobbyTable.start) {
          (lobbyTable +: tables, Right(lobbyTable))
        } else {
          val updatedTables = tables.span(_.id == afterId) match {
            case (Nil, ys) => ys :+ lobbyTable
            case (xs, ys) => xs ++ Seq(lobbyTable) ++ ys
          }
          (updatedTables, Right(lobbyTable))
        }
      }
    }.flatMap(Sync[F].fromEither)
}

  override def update(lobbyTable: LobbyTable): F[Option[LobbyTable]] = {
    cache.modify { tables =>
      val index = tables.indexWhere(_.id == lobbyTable.id)
      if (index == -1) (tables, None)
      else (tables.updated(index, lobbyTable), Some(lobbyTable))
    }
  }

  //TODO should fail if no id found
  override def delete(id: LobbyTable.Id): F[Unit] = {
    cache.update(_.filter(_.id != id))
  }
}

object InMemoryLobbyTableStorage {
  def apply[F[_]: Sync]: F[InMemoryLobbyTableStorage[F]] = {
    import cats.syntax.functor._

    Ref.apply[F].of(Seq.empty[LobbyTable]).map{ cache =>
      new InMemoryLobbyTableStorage(cache)
    }
  }
}
