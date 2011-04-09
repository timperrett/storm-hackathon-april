package teamawesome.comet

import scala.xml.{NodeSeq,Text}
import net.liftweb._,
  util.Helpers._,
  http.SHtml,
  http.js.JsCmds.{Noop,SetHtml,Run}
import akka.actor.Actor.registry
import teamawesome.actor._

class Search extends AkkaCometActor {
  
  override def lowPriority = {
    case WorkingInBackground(msg) => 
      partialUpdate(SetHtml("status", Text("Stalking you...")))
    case Result(t,c) => 
      partialUpdate(
        Run("""addnewcontent(<div class="box">Sample</div>)"""))
  }
  
  def render =
    "type=text" #> SHtml.ajaxText("", v => {
      println("++++++++++++ Sending query %s".format(v))
      registry.actorFor[SearchManager].map(_ ! DetermineQueryType(Query(v)))
      Noop
    })
}
