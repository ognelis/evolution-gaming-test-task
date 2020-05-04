package com.evolutiongaming.maqexoju.components.storage.impl

import cats.effect.IO
import com.evolutiongaming.maqexoju.domain.model.{LobbyTable, LobbyTables}
import org.scalatest.FreeSpec

class UserStorageTest extends FreeSpec {

  "InMemoryLobbyTableStorage" - {

    "should correctly create one table" in {
      import cats.syntax.flatMap._
      import cats.syntax.functor._

      val lobbyTable = LobbyTable(
        id = LobbyTable.Id(1L),
        name = "blackjack europe",
        participants = 7,
      )

      val expectedLobbyTables = LobbyTables(
        tables = Seq(
          lobbyTable,
        )
      )

      val afterId = LobbyTable.Id(-1L)

      (for {
        tableStorage <- InMemoryLobbyTableStorage[IO]
        _ <- tableStorage.create(lobbyTable, afterId)
        tables <- tableStorage.all
      } yield {
        assert(tables == expectedLobbyTables)
      }).unsafeRunSync()
    }

    "shouldn't create one table if start id is illegal" in {
      import cats.syntax.flatMap._
      import cats.syntax.functor._

      val lobbyTable = LobbyTable(
        id = LobbyTable.Id(1L),
        name = "blackjack europe",
        participants = 7,
      )

      val expectedLobbyTables = LobbyTables(
        tables = Seq(
          lobbyTable,
        )
      )

      val afterId = LobbyTable.Id(0L)

      val failedOp = (for {
        tableStorage <- InMemoryLobbyTableStorage[IO]
        _ <- tableStorage.create(lobbyTable, afterId)
      } yield ())

      assertThrows[IllegalArgumentException]{
        failedOp.unsafeRunSync()
      }
    }

    "should correctly create many table to beginning" in {
      import cats.syntax.flatMap._
      import cats.syntax.functor._

      val startId = LobbyTable.Id(-1L)

      val lobbyTable1 = LobbyTable(
        id = LobbyTable.Id(1L),
        name = "blackjack europe",
        participants = 7,
      )

      val lobbyTable2 = LobbyTable(
        id = LobbyTable.Id(2L),
        name = "blackjack minsk",
        participants = 15,
      )

      val lobbyTable3 = LobbyTable(
        id = LobbyTable.Id(3L),
        name = "blackjack novosibirsk",
        participants = 1,
      )

      val expectedLobbyTables = LobbyTables(
        tables = Seq(
          lobbyTable3, lobbyTable2, lobbyTable1,
        )
      )

      (for {
        tableStorage <- InMemoryLobbyTableStorage[IO]
        _ <- tableStorage.create(lobbyTable1, startId)
        _ <- tableStorage.create(lobbyTable2, startId)
        _ <- tableStorage.create(lobbyTable3, startId)
        tables <- tableStorage.all
      } yield {
        assert(tables == expectedLobbyTables)
      }).unsafeRunSync()
    }

    "should correctly create many table to end" in {
      import cats.syntax.flatMap._
      import cats.syntax.functor._

      val startId = LobbyTable.Id(-1L)

      val lobbyTable1 = LobbyTable(
        id = LobbyTable.Id(1L),
        name = "blackjack europe",
        participants = 7,
      )

      val lobbyTable2 = LobbyTable(
        id = LobbyTable.Id(2L),
        name = "blackjack minsk",
        participants = 15,
      )
      val afterTable1 = LobbyTable.Id(1L)

      val lobbyTable3 = LobbyTable(
        id = LobbyTable.Id(3L),
        name = "blackjack novosibirsk",
        participants = 1,
      )
      val afterTable2 = LobbyTable.Id(2L)

      val expectedLobbyTables = LobbyTables(
        tables = Seq(
          lobbyTable1, lobbyTable2, lobbyTable3,
        )
      )

      (for {
        tableStorage <- InMemoryLobbyTableStorage[IO]
        _ <- tableStorage.create(lobbyTable1, startId)
        _ <- tableStorage.create(lobbyTable2, afterTable1)
        _ <- tableStorage.create(lobbyTable3, afterTable2)
        tables <- tableStorage.all
      } yield {
        assert(tables == expectedLobbyTables)
      }).unsafeRunSync()
    }


    "should correctly create many table and one of in middle" in {
      import cats.syntax.flatMap._
      import cats.syntax.functor._

      val expectedLobbyTable1 = LobbyTable(
        id = LobbyTable.Id(1L),
        name = "blackjack europe",
        participants = 7,
      )
      val startId = LobbyTable.Id(-1L)

      val expectedLobbyTable2 = LobbyTable(
        id = LobbyTable.Id(2L),
        name = "blackjack minsk",
        participants = 15,
      )
      val afterTable1 = LobbyTable.Id(1L)

      val expectedLobbyTable3 = LobbyTable(
        id = LobbyTable.Id(3L),
        name = "blackjack novosibirsk",
        participants = 1,
      )
      val beforeTable2 = LobbyTable.Id(1L)

      val expectedLobbyTables = LobbyTables(
        tables = Seq(
          expectedLobbyTable1, expectedLobbyTable3, expectedLobbyTable2,
        )
      )

      (for {
        tableStorage <- InMemoryLobbyTableStorage[IO]
        _ <- tableStorage.create(expectedLobbyTable1, startId)
        _ <- tableStorage.create(expectedLobbyTable2, afterTable1)
        _ <- tableStorage.create(expectedLobbyTable3, beforeTable2)
        tables <- tableStorage.all
      } yield {
        assert(tables == expectedLobbyTables)
      }).unsafeRunSync()
    }
  }

}
