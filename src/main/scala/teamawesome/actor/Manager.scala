package teamawesome.actor

import akka.actor.Actor

case class Query(content: String)

class SearchManager extends Actor {
  def receive = {
    case (s: String) => println(s)
  }
}
