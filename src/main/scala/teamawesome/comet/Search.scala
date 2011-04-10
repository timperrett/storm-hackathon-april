package teamawesome.comet

import scala.xml.{NodeSeq,Text}
import akka.actor.Actor.registry
import teamawesome.actor.{AkkaCometActor,QueryDispatcher,MessageToUser,NewQuery}
import teamawesome.lib._
import net.liftweb._,
  util.Helpers._,
  http.{SHtml,S},
  http.js.JsCmds.{Noop,SetHtml,Run},
  http.js.JE.JsRaw

class Search extends AkkaCometActor {
  
  private var facts: List[Fact[_]] = Nil
  
  override def lowPriority = {
    case MessageToUser(Some(msg)) => 
      partialUpdate(SetHtml("modal_msg", Text(msg)))
      
    case Some(Discovered(what)) => {
      println("------------------------------")
      // send new events back into the system
      val newFacts = facts diff what
      // newFacts.foreach(f => )
      facts = (facts ::: what).distinct
      
      partialUpdate(Run("appendDiscovery('%s')".format(what.map(_.data.toDisplay).mkString)))
    }
  }
  
  def render = 
    "type=text" #> S.callOnce {
      SHtml.ajaxText("Who do you want to stalk today?", v => {
        registry.actorFor[QueryDispatcher].map {
          _ ! NewQuery(Query(v))
        }
        Run("prepareResultsUI()")
      })
    } 
}
