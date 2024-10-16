package modules

import com.google.inject.{AbstractModule, Provides}
import play.api.{Configuration, Environment}
import roulette.{ControllerInterface, Controller}
import javax.inject.{Singleton, Inject}
import scala.concurrent.ExecutionContext

class RouletteModule(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def provideController(executionContext: ExecutionContext): ControllerInterface = {
    new Controller()(executionContext)
  }
}