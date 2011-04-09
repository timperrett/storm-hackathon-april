package teamawesome.actor

import akka.actor.Actor

class Stalker extends Actor {
  def receive = {
    case Process(v, f) => f(v)
  }
}


