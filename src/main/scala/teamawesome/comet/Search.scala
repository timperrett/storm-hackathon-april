package teamawesome.comet

import scala.xml.{NodeSeq,Text}
import akka.actor.Actor.registry
import teamawesome.actor.{AkkaCometActor,QueryDispatcher,WorkingInBackground,DetermineQueryType}
import teamawesome.lib.{Query,Discovered,Fact}
import net.liftweb._,
  util.Helpers._,
  http.{SHtml,S},
  http.js.JsCmds.{Noop,SetHtml,Run},
  http.js.JE.JsRaw

class Search extends AkkaCometActor {
  
  private var facts: List[Fact[_]] = Nil
  
  override def lowPriority = {
    case WorkingInBackground(msg) => 
      partialUpdate(SetHtml("modal_msg", Text("Prowling...")))
    
    case Some(Discovered(what)) => {
      println("------------------------------")
      // send new events back into the system
      val newFacts = facts diff what
      // newFacts.foreach(f => )
      
      facts = (facts ::: what).distinct
      partialUpdate(Run("addNewContent('ZZZ')"))
      // partialUpdate(Run("""addnewcontent('%s')""".format(what.map(_.data.toString + ", "))))
      // partialUpdate(SetHtml("modal_msg", Text("ZZZZZZZZZ")))
    }
  }
  
  def render = 
    "type=text" #> S.callOnce {
      SHtml.ajaxText("Who do you want to stalk today?", v => {
        println("++++++++++++ Sending query %s".format(v))
        registry.actorFor[QueryDispatcher].map(_ ! DetermineQueryType(Query(v)))
        Run("$('#modal').overlay({ top: '30%', closeOnClick: false, load: true }); $('#web1').fadeOut();")
      })
    } 
}
