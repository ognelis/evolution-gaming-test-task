package com.evolutiongaming.maqexoju

import cats.effect.Sync
import com.evolutiongaming.maqexoju.components.auth.impl.BasicAuthServiceImpl
import com.evolutiongaming.maqexoju.components.server.HttpServer
import com.typesafe.config.{Config, ConfigRenderOptions}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import pureconfig.generic.auto._
import pureconfig.generic.semiauto.deriveConvert
import pureconfig.{ConfigConvert, ConfigSource, ConfigWriter}

case class MaqexojuConfig(
                           auth: BasicAuthServiceImpl.Config,
                           server: HttpServer.Config,
)

object MaqexojuConfig {

  implicit val configConvert: ConfigConvert[MaqexojuConfig] = deriveConvert

  def apply[F[_] : Sync](config: Config): F[MaqexojuConfig] = {
    import cats.syntax.apply._
    import cats.syntax.flatMap._

    Slf4jLogger.create[F].flatMap { loggerF =>
      ConfigSource.fromConfig(config).at("maqexoju").load[MaqexojuConfig] match {
        case Right(config) =>
          val renderOptions = ConfigRenderOptions.defaults().setOriginComments(false)
          val renderedConfig = ConfigWriter[MaqexojuConfig].to(config).render(renderOptions)
          loggerF.info(s"Application config:\n$renderedConfig") *>
            Sync[F].pure(config)

        case Left(failures) =>
          loggerF.error(s"Application config is invalid: $failures") *>
            Sync[F].raiseError[MaqexojuConfig](new RuntimeException("Application config is invalid"))
      }
    }
  }

}
