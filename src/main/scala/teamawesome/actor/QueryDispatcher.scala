package teamawesome.actor

import scala.xml.NodeSeq
import akka.actor.Actor
import akka.actor.Actor.registry
import teamawesome.lib.{Query, QueryAugmentation, ServiceClient, Discovered}
import teamawesome.lib.QueryIdentifier._

case class DetermineQueryType(content: Query)
case class Process(q: Query, f: Query => Option[Discovered])

class QueryDispatcher extends Actor with QueryAugmentation {
  import ServiceClient._
  
  def receive = {
    // send stalker results back to the comet actor
    // case Some(r@Result(t, content)) => self.reply(r)
    
    // determine query type
    case DetermineQueryType(query: Query) => {
      val stalker = registry.actorFor[Stalker]
      val queryTypes = List(IsEmailAddress, IsTwitterUsername, IsWebsite)
      
      queryTypes.map(_(query.content)).filterNot(_.isEmpty) match {
        case List(r,_*) => r match {
          case Some(t: TwitterUsername.type) => 
            stalker.map(_ ! Process(query, Twitter))
          case Some(t: EmailAddress.type) => {
            stalker.map(_ ! Process(query, WhoisFromEmail))
            stalker.map(_ ! Process(query, EmailToPhotos))
          }
          
//          case Some(t: Website.type) => stalker.map(_ ! Process(query, ServiceFunctionRegistry.WebsiteTo))
          
          case _ => 
        }
      }
      self.reply(WorkingInBackground(Some("Stalking you...")))
    }
  }
}
