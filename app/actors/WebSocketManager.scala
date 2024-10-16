package actors

import org.apache.pekko.actor.{Actor, ActorRef, Props}
import play.api.libs.json.JsValue

object WebSocketManager {
  case class Register(out: ActorRef)
  case class Unregister(out: ActorRef)
  case class Broadcast(message: JsValue)

  def props = Props(new WebSocketManager)
}

class WebSocketManager extends Actor {
  import WebSocketManager._

  var clients: Set[ActorRef] = Set.empty

  def receive: Receive = {
    case Register(out) =>
      clients += out
    case Unregister(out) =>
      clients -= out
    case Broadcast(message) =>
      clients.foreach(_ ! message)
  }
}