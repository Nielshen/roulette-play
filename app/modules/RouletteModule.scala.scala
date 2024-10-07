package modules

import com.google.inject.{AbstractModule, Provides}
import play.api.{Configuration, Environment}
import Roulette.controller.controllerComponent.ControllerInterface
import Roulette.controller.controllerComponent.controllerBaseImpl.Controller
import Roulette.fileIO.FileIOInterface
import Roulette.fileIO.xmlImpl.FileIO
import Roulette.db.dao.{
  PlayerDAO,
  BetDAO,
  MongoDBPlayerDAO,
  MongoDBBetDAO,
  SlickPlayerDAO,
  SlickBetDAO
}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import reactivemongo.api.{AsyncDriver, MongoConnection}
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Singleton
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure

class RouletteModule(environment: Environment, configuration: Configuration)
    extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[FileIOInterface]).to(classOf[FileIO]).in(classOf[Singleton])
    bind(classOf[ControllerInterface])
      .to(classOf[Controller])
      .in(classOf[Singleton])
  }

  @Provides
  @Singleton
  def providePlayerDAO(
      configuration: Configuration,
      executionContext: ExecutionContext
  ): PlayerDAO = {
    implicit val ec: ExecutionContext = executionContext

    if (configuration.get[Boolean]("db.useSlick")) {
      val dbConfig = Database.forConfig("db.slick.dbs.default.db")
      new SlickPlayerDAO(dbConfig)
    } else {
      val mongoUri = configuration.get[String]("mongodb.uri")
      val driver = AsyncDriver()
      val connectionFuture = MongoConnection.fromString(mongoUri)
      val daoFuture = connectionFuture.flatMap({ parsedUri =>
        driver.connect(parsedUri).map { conn =>
          new MongoDBPlayerDAO("roulette", "players")(ec, conn)
        }
      })
      Await.result(daoFuture, 10.seconds)
    }
  }

  @Provides
  @Singleton
  def provideBetDAO(
      configuration: Configuration,
      executionContext: ExecutionContext
  ): BetDAO = {
    implicit val ec: ExecutionContext = executionContext

    if (configuration.get[Boolean]("db.useSlick")) {
      val dbConfig = Database.forConfig("db.slick.dbs.default.db")
      new SlickBetDAO(dbConfig)
    } else {
      val mongoUri = configuration.get[String]("mongodb.uri")
      val driver = AsyncDriver()
      val connectionFuture = MongoConnection.fromString(mongoUri)
      val daoFuture = connectionFuture.flatMap({ parsedUri =>
        driver.connect(parsedUri).map { conn =>
          new MongoDBBetDAO("roulette", "bets")(ec, conn)
        }
      })
      Await.result(daoFuture, 10.seconds)
    }
  }
}
