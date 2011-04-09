package teamawesome.actor

import scala.xml.{NodeSeq,Text => NodeText}
import akka.actor.Actor

case class Query(content: String)
case class DetermineQueryType(content: Query)
case class Process(q: Query, f: Query => Option[Result])
case class Result(t: MediaType, content: NodeSeq)

trait MediaType
case object Text extends MediaType
case object Video extends MediaType
case object Image extends MediaType

import akka.actor.Actor.registry

trait QueryIdentifier
case object TwitterUsername extends QueryIdentifier
case object EmailAddress extends QueryIdentifier
case object Website extends QueryIdentifier

// front end notifications
case class WorkingInBackground(msg: Option[String])

class SearchManager extends Actor {
  
  def receive = {
    
    // send stalker results back to the comet actor
    case Some(r@Result(t, content)) => self.reply(r)
      
    // determine query type
    case DetermineQueryType(query: Query) => {
      val stalker = registry.actorFor[Stalker]
      
      val IsTwitterUsername: String => Option[QueryIdentifier] = x => x.toSeq match {
        case Seq('@', _*) => Some(TwitterUsername)
        case _ => None
      }
      
      val IsEmailAddress: String => Option[QueryIdentifier] =  "^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$".r.findFirstIn(_) match {
        case Some(_) => Some(EmailAddress)
        case _ => None
      }
      
      val IsWebsite: String => Option[QueryIdentifier] =  "^http://".r.findFirstIn(_) match {
        case Some(_) => Some(Website)
        case _ => None
      }
      
      val queryTypes = List(IsEmailAddress, IsTwitterUsername, IsWebsite)
      
      queryTypes.map(_(query.content)).filter(!_.isEmpty) match {
        case List(r,_*) => r match {
        	
          case Some(t: TwitterUsername.type) => stalker.map(_ ! Process(query, ServiceFunctionRegistry.Twitter))
          
          case Some(t: EmailAddress.type) => {
        	  stalker.map(_ ! Process(query, ServiceFunctionRegistry.WhoisFromEmail))
        	  stalker.map(_ ! Process(query, ServiceFunctionRegistry.EmailToPhotos))
          }
          
//          case Some(t: Website.type) => stalker.map(_ ! Process(query, ServiceFunctionRegistry.WebsiteTo))
          
          case _ => 
        }
      }
      self.reply(WorkingInBackground(Some("Stalking you...")))
    }
  }
}
