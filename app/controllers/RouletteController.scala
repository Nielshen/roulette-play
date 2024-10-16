package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import roulette.{ControllerInterface, Player}
import org.apache.pekko.actor.{Actor, ActorRef, ActorSystem, Props} //Using Apache Pekko because akka is deprecated!
import org.apache.pekko.stream.Materializer
import play.api.libs.streams.ActorFlow
import org.apache.pekko.actor.Actor

import scala.concurrent.{ExecutionContext, Future}
import actors.WebSocketManager

class RouletteController @Inject()(val controllerComponents: ControllerComponents, rouletteController: ControllerInterface)(implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends BaseController {

  val webSocketManager = system.actorOf(WebSocketManager.props)

  implicit val playerWrites: Writes[Player] = new Writes[Player] {
    def writes(player: Player): JsValue = Json.obj(
      "player_index" -> player.player_index,
      "available_money" -> player.available_money
    )
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    val players = rouletteController.getPlayers
    Ok(views.html.index(players))
  }

  def rules() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.rules())
  }
  
    def placeBet() = Action.async(parse.json) { implicit request =>
    val result = for {
        playerIndex <- (request.body \ "playerIndex").validate[Int]
        betType <- (request.body \ "betType").validate[String]
        betValue <- (request.body \ "betValue").validate[String]
        betAmount <- (request.body \ "betAmount").validate[Int]
    } yield {
        rouletteController.createAndAddBet(
        playerIndex,
        betType,
        if (betType == "n") Some(betValue.toInt) else None,
        if (betType == "e") Some(betValue) else None,
        if (betType == "c") Some(betValue) else None,
        betAmount
        ).flatMap { success =>
        if (success) {
            Future.successful(Ok(Json.obj("players" -> Json.toJson(rouletteController.getPlayers))))
        } else {
            Future.successful(BadRequest(Json.obj("message" -> "Failed to place bet")))
        }
        }
    }

    result.fold(
        errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
        identity
    )
    }

  def calculateBets() = Action.async { implicit request =>
    Future {
      val results = rouletteController.calculateBets()
      Ok(Json.obj(
        "results" -> Json.toJson(results),
        "players" -> Json.toJson(rouletteController.getPlayers)
      ))
    }
  }

    def socket = WebSocket.accept[JsValue, JsValue] { request =>
        ActorFlow.actorRef { out =>
        println("Verbindung empfangen")
        webSocketManager ! WebSocketManager.Register(out)
        RouletteWebSocketActor.props(out, rouletteController, webSocketManager)
        }
    }

  object RouletteWebSocketActor {
    def props(out: ActorRef, rouletteController: ControllerInterface, webSocketManager: ActorRef): Props =
      Props(new RouletteWebSocketActor(out, rouletteController, webSocketManager))
  }

  class RouletteWebSocketActor(out: ActorRef, rouletteController: ControllerInterface, webSocketManager: ActorRef) extends Actor {
    def receive: Receive = {
      case message: JsValue =>
        (message \ "action").as[String] match {
          case "placeBet" =>
            val playerIndex = (message \ "playerIndex").as[Int]
            val betType = (message \ "betType").as[String]
            val betValue = (message \ "betValue").as[String]
            val betAmount = (message \ "betAmount").as[Int]

            val success = rouletteController.createAndAddBet(
              playerIndex,
              betType,
              if (betType == "n") Some(betValue.toInt) else None,
              if (betType == "e") Some(betValue) else None,
              if (betType == "c") Some(betValue) else None,
              betAmount
            )

            val result = Json.obj("action" -> "betPlaced", "players" -> Json.toJson(rouletteController.getPlayers))
            webSocketManager ! WebSocketManager.Broadcast(result)

          case "calculateBets" =>
            val results = rouletteController.calculateBets()
            val result = Json.obj(
              "action" -> "betsCalculated",
              "results" -> Json.toJson(results),
              "players" -> Json.toJson(rouletteController.getPlayers)
            )
            webSocketManager ! WebSocketManager.Broadcast(result)
            
        case "spinWheel" =>
          val rotation = (message \ "rotation").as[Double]
          val result = Json.obj("action" -> "spinWheel", "rotation" -> rotation)
          webSocketManager ! WebSocketManager.Broadcast(result)

          case _ =>
            out ! Json.obj("action" -> "error", "message" -> "Unknown action")
        }
    }
    override def postStop(): Unit = {
      webSocketManager ! WebSocketManager.Unregister(out)
    }
  }
}