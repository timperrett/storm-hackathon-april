package teamawesome.comet

import scala.xml.{NodeSeq,Text}
import akka.actor.Actor.registry
import teamawesome.actor.{AkkaCometActor,QueryDispatcher,WorkingInBackground,DetermineQueryType}
import teamawesome.lib.{Query,Discovered}
import net.liftweb._,
  util.Helpers._,
  http.SHtml,
  http.js.JsCmds.{Noop,SetHtml,Run}

class Search extends AkkaCometActor {
  
  override def lowPriority = {
    case s : String => 
      partialUpdate(SetHtml("status", Text("XXXXXXXXXXXXXXXXXXXX" + s)))
    case WorkingInBackground(msg) => 
      partialUpdate(SetHtml("status", Text("Stalking you...")))
    case Some(Discovered(what)) => 
      partialUpdate(SetHtml("status", Text("ZZZZZZZZZ")))
    case unknown => println("+++++++ " + unknown)
    
      // partialUpdate(
        // Run("""addnewcontent(<div class="box">Sample</div>)""")
      // )
  }
  
  def render =
    "type=text" #> SHtml.ajaxText("", v => {
      println("++++++++++++ Sending query %s".format(v))
      registry.actorFor[QueryDispatcher].map(_ ! DetermineQueryType(Query(v)))
      Noop
    })
}
