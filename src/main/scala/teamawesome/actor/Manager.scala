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
    
    val whoisClient = new WhoisClient
    whoisClient.connect("whois.opensrs.net", 43)
    val results = whoisClient.query(q.content)
    
    var address = results
    				.split("\r\n\r\n")
    				.toList
    				.filter( _.toLowerCase.contains("address") )
    				.mkString("\n\n")
            
    println("<><<><><><><><><><><><><><><><><><><><>" + address)
    
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
