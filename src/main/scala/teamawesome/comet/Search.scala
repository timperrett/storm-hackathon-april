package teamawesome.comet

import scala.xml.NodeSeq
import net.liftweb._,
  util.Helpers._,
  http.{CometActor,SHtml},
  http.js.JsCmds.Alert

class Search extends CometActor {
  def render =
    "type=text" #> SHtml.ajaxText("Whatever", value => Alert("W00T"))
}
