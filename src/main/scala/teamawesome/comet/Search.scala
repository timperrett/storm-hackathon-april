package teamawesome.comet

import scala.xml.NodeSeq
import net.liftweb._,
  util.Helpers._,
  http.{CometActor,SHtml},
  http.js.JsCmds.Noop
import akka.actor.Actor.registry
import teamawesome.actor.{SearchManager,Query}

class Search extends CometActor {
  def render =
    "type=text" #> SHtml.ajaxText("", v => {
      println("++++++++++++ Sending query %s".format(v))
      registry.actorFor[SearchManager].map(_ ! Query(v))
      Noop
    })
}
