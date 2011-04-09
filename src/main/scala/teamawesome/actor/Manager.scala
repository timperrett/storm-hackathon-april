package teamawesome.actor

import scala.xml.NodeSeq
import akka.actor.Actor

import org.apache.commons.net.whois.WhoisClient

case class Query(content: String)
case class Process(q: Query, f: Query => Option[Result])
case class Result(t: MediaType, content: NodeSeq)
case class WorkingInBackground(msg: Option[String])

trait MediaType
case object Text extends MediaType
case object Video extends MediaType
case object Image extends MediaType

object ServiceFunctionRegistry {
  type ⊛ = Query => Option[Result]

  val WhoIs: ⊛ = q => {
    println(q.content)

    val whoisClient = new WhoisClient
    
    whoisClient.connect("whois.namejuice.com", 43)
    
    val results = whoisClient.query( q.content )

    println("_+_+_+_+_+_+_+_++?>>>>> " + results )
    
    None
  }

  val Twitter: ⊛ = q => {
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
        _ !!! Process(value, ServiceFunctionRegistry.WhoIs)
      }
      self.reply(WorkingInBackground(Some("Stalking you...")))
    }
  }
}
