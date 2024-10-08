package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import roulette.{ControllerInterface, Player}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RouletteController @Inject()(val controllerComponents: ControllerComponents, rouletteController: ControllerInterface) extends BaseController {

    implicit val playerWrites: Writes[Player] = new Writes[Player] {
        def writes(player: Player): JsValue = Json.obj(
        "player_index" -> player.player_index,
        "available_money" -> player.available_money
        )
    }

    def index() = Action { implicit request: Request[AnyContent] =>
        val players = rouletteController.getPlayers
        //Ok(views.html.basicTui(players)) //Test GUI
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
            ).map { success =>
            if (success) {
                Ok(Json.obj("players" -> Json.toJson(rouletteController.getPlayers)))
            } else {
                BadRequest(Json.obj("message" -> "Failed to place bet"))
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


}