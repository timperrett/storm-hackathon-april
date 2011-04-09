package teamawesome.actor

import scala.xml.NodeSeq
import akka.actor.Actor

case class Query(content: String)
case class Process(q: Query, f: Query => Option[Result])

case class Result(t: MediaType, content: NodeSeq)

trait MediaType
case object Text extends MediaType
case object Video extends MediaType
case object Image extends MediaType

object ServiceFunctionRegistry { 
  type ⊛ = Query => Option[Result]
  val WhoIs: ⊛ = q => {
    println("*******************")
    None
  }
}

import akka.actor.Actor.registry

class SearchManager extends Actor {
  def receive = {
    case value: Query => {
      println("********* Recieved: " + value.toString)
      // send to worker
      registry.actorFor[Stalker].map {
        _ ! Process(value, ServiceFunctionRegistry.WhoIs)
      }
    }
  }
}

class Stalker extends Actor {
  def receive = {
    case Process(v,f) => f(v)
  }
}