package com.evolutiongaming.maqexoju

import org.http4s.circe.jsonEncoderWithPrinter
import cats.effect.{Async, Blocker, ExitCode, IO, IOApp, Resource}
import com.evolutiongaming.maqexoju.components.auth.impl.BasicAuthServiceImpl
import com.evolutiongaming.maqexoju.components.server.HttpServer
import com.evolutiongaming.maqexoju.components.storage.impl.{InMemoryLobbyTableStorage, UserStorageStub}
import com.evolutiongaming.maqexoju.components.util.ExecutionContexts
import com.typesafe.config.{Config, ConfigFactory}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.{Json, Printer}
import org.http4s.EntityEncoder

object Maqexoju extends IOApp {

  type F[A] = IO[A]

  override def run(args: List[String]): F[ExitCode] = {
    import cats.syntax.functor._
    import cats.syntax.flatMap._

    val resources = for {
      loggerF <- Resource.liftF(Slf4jLogger.create[F])
      _ <- Resource.liftF(loggerF.info("Maqexoju started!"))

      blockingEc <- ExecutionContexts.cachedThreadPool[F]
      blocker = Blocker.liftExecutionContext(blockingEc)

      conf <- Resource.liftF(blocker.delay[F, Config](ConfigFactory.load()))
      appConfig <- Resource.liftF(MaqexojuConfig[F](conf))
    } yield appConfig

    resources.use { appConfig =>
      implicit val jsonEncoder: EntityEncoder[F, Json] = jsonEncoderWithPrinter(Printer.spaces2)

      val userStorage = new UserStorageStub[F]

      val authService = BasicAuthServiceImpl.withJsonResponse[F](
        config = appConfig.auth,
        userStorage = userStorage,
      )

      for {
        lobbyTableStorage <- InMemoryLobbyTableStorage[F]
        exitCode <- HttpServer[F](
          config = appConfig.server,
          authService = authService,
          lobbyTableStorage = lobbyTableStorage,
        ).use(_ => Async[F].never.as(ExitCode.Success))
      } yield exitCode
    }
  }
}
