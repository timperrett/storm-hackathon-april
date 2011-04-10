package teamawesome.lib

import org.apache.commons.net.whois.WhoisClient

import dispatch._
import Http._
import json.Js._
import twitter._

import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._

object ServiceClient {
  type ⊛ = Query => Option[Discovered]
  
  val WhoIs: ⊛ = q => {
    val address = (try {
      val whoisClient = new WhoisClient
      whoisClient.connect("whois.opensrs.net", 43)
      Some(whoisClient.query(q.content))
    } catch {
      case e => None
    }).flatMap { 
      _.split("\r\n\r\n").toList.filter(
        _.toLowerCase.contains("address")) match {
          case List(head, _*) => Some(head)
          case Nil => None
        }
    }
    
    address.foreach { a => 
      println(">>>>>>>>>>>>>>>>>> ADDRESS: " + a)
      println(">>>>>>>>>>>>>>>>>> MAP: " + "http://maps.google.com/maps/api/staticmap?center="+ a.replaceAll("\n", ", ") +"&zoom=16&size=512x256&maptype=roadmap&sensor=false")
    }
    None
  }

  val Twitter: ⊛ = q => {
    def withContentFrom[T](url: String)(f: String => T): T = new Http().apply(new Request(url) >- f) 
    def removeSpan(l: String): String  = l.replaceAll(".*\">", "").replaceAll("<.*", "")
    def getHref(l: String): String  = l.replaceAll(".*href=\"", "").replaceAll("\".*", "")
    
    val result = withContentFrom("http://twitter.com/" + q.content.substring(1)) { html =>
      html.split("\n").filter( _.toLowerCase.contains("<span class=\"label\">name</span>") ).foreach(l => println (">>>>>>>>>>>>>>> FULL NAME: " + removeSpan(l)))
      html.split("\n").filter( _.toLowerCase.contains("<span class=\"label\">bio</span>") ).foreach(l => println (">>>>>>>>>>>>>>> BIO: " + removeSpan(l)))
      html.split("\n").filter( _.toLowerCase.contains("<span class=\"label\">web</span>") ).foreach(l => println (">>>>>>>>>>>>>>> URL: " + getHref(l)))
      html.split("\n").filter( _.toLowerCase.contains("<span class=\"label\">location</span>") ).foreach( l => println (">>>>>>>>>>>>>>> LOCATION: " + removeSpan(l)))
      html.split("\n").filter( _.toLowerCase.contains("<span class=\"label\">location</span>") ).foreach( l => println (">>>>>>>>>>>>>>> MAP: " + removeSpan(l).replaceAll("\n", ", ") +"&zoom=16&size=512x256&maptype=roadmap&sensor=false"))
    }
    
    None
  }

  val WhoisFromEmail: ⊛ = q =>
    WhoIs(q.copy(content = q.content.split("@").last))
  
  private def socialJsonFor(q: Query) = 
    Http(
      :/("socialgraph.googleapis.com") / "otherme" <<? 
        Map("q" -> q.content) >- JsonParser.parse)
  
  private def getFacts(q: Query, field: String) = 
    try {
      Some(Discovered(for {
        JObject(other) <- socialJsonFor(q)
        JField(_, JObject(profile)) <- other
        JField(field, JString(f)) <- profile
      } yield Fact(f)))
    } catch {
      case e => None
    }
  
  val URLsFromEmail: ⊛ = q => getFacts(q,"url")
  
  val PhotosFromEmail: ⊛ = q => getFacts(q,"photo")

}
