package teamawesome.comet

import scala.xml.{NodeSeq,Text}
import akka.actor.Actor.registry
import teamawesome.actor.{AkkaCometActor,QueryDispatcher,WorkingInBackground,DetermineQueryType}
import teamawesome.lib.{Query,Discovered,Fact}
import net.liftweb._,
  util.Helpers._,
  http.SHtml,
  http.js.JsCmds.{Noop,SetHtml,Run},
  http.js.JE.JsRaw

class Search extends AkkaCometActor {
  
  private var facts: List[Fact[_]] = Nil
  
  override def lowPriority = {
    case WorkingInBackground(msg) => 
      partialUpdate(SetHtml("status", Text("Stalking you...")))
    case Some(Discovered(what)) => {
      what.foreach(println)
      // send new events back into the system
      val newFacts = facts diff what
      
      facts = (facts ::: what).distinct
      
      partialUpdate(Run("""addnewcontent('%s')""".format(what.map(_.data.toString + ", "))))
      // partialUpdate(SetHtml("status", Text("ZZZZZZZZZ")))
    }
  }
  
  def render = {
    var query: String = ""
    "type=text" #> SHtml.text("", x => query = x) &
    "type=submit" #> SHtml.ajaxSubmit("Stalk!", () => {
      registry.actorFor[QueryDispatcher].map(_ ! DetermineQueryType(Query(query)))
      Noop
    }) andThen SHtml.makeFormsAjax
  }
}
