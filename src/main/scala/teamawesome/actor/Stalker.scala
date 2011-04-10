package teamawesome.actor

import akka.actor.Actor
import teamawesome.lib.{Discovered,Fact}

class Stalker extends Actor {
  def receive = {
    case Process(v, f) => self.reply(f(v))
  }
}
