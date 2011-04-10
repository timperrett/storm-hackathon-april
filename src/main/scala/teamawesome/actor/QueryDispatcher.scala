package teamawesome.actor

import scala.xml.NodeSeq
import akka.actor.{Actor,PoisonPill}
import akka.actor.Actor.actorOf
import teamawesome.lib.{Query, QueryAugmentation, ServiceClient, Discovered}
import teamawesome.lib.QueryIdentifier._

case class NewQuery(content: Query)
case class Process(q: Query, f: Query => Option[Discovered[_]])

class QueryDispatcher extends Actor with QueryAugmentation {
  import ServiceClient._
  
  def receive = {
    case NewQuery(query: Query) => {
      val stalker = actorOf[Stalker].start
      val queryTypes = List(IsEmailAddress, IsTwitterUsername, IsWebsite)
      
      queryTypes.map(_(query.content)).filterNot(_.isEmpty) match {
        case List(r,_*) => r match {
          case Some(t: TwitterUsername.type) => 
            stalker.forward(Process(query, Twitter))
          case Some(t: EmailAddress.type) => {
            stalker.forward(Process(query, WhoisFromEmail))
            stalker.forward(Process(query, PhotosFromEmail))
            stalker.forward(Process(query, URLsFromEmail))
          }
          
          //case Some(t: Website.type) => stalker.map(_ ! Process(query, ServiceFunctionRegistry.WebsiteTo))
          
          case _ => 
        }
      }
      // notify the work it needs to shutdown
      stalker ! PoisonPill
      // notify the front end that stuff is happening
      // self.reply(MessageToUser(Some("Prowling...")))
    }
  }
}
