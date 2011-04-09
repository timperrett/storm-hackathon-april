package teamawesome.actor;

import org.apache.commons.net.whois.WhoisClient

import dispatch._
import Http._
import json.Js._
import twitter._

import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._

object ServiceFunctionRegistry {
  type ⊛ = Query => Option[Result]

  val WhoIs: ⊛ = q => {

    val whoisClient = new WhoisClient
    whoisClient.connect("whois.opensrs.net", 43)
    val results = whoisClient.query(q.content)

    var address = results
      .split("\r\n\r\n")
      .toList
      .filter(_.toLowerCase.contains("address")) match {
        case List(head, _) => head
      }

    println(">>>>>>>>>>>>>>>>>> ADDRESS: " + address)

    println(">>>>>>>>>>>>>>>>>> MAP: " + "http://maps.google.com/maps/api/staticmap?center="+ address.replaceAll("\n", ", ") +"&zoom=16&size=512x256&maptype=roadmap&sensor=false")
    
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

  val EmailToPhotos: ⊛ = q => {
    val email = q.content

    //https://socialgraph.googleapis.com/otherme?q=tperrett@gmail.com

    val req = :/("socialgraph.googleapis.com") / "otherme" <<? Map("q" -> q.content)

    val jsonResp = Http(req >- JsonParser.parse)

    val photos = for {
      JObject(otherMe) <- jsonResp
      JField(_, JObject(profileObj)) <- otherMe
      JField("photo", JString(photo)) <- profileObj
    } yield photo

    println
    photos.foreach(l => println(">>>>>>>>>>>>>>>>>> PHOTO: " + l))

    val urls = for {
      JObject(otherMe) <- jsonResp
      JField(_, JObject(profileObj)) <- otherMe
      JField("url", JString(url)) <- profileObj
    } yield url

    println
    urls.foreach(l => println(">>>>>>>>>>>>>>>>>> URL: " + l))
    
    urls.filter( _.contains("twitter") ).map( _.replaceAll(".*/", "") ).foreach( l => println(">>>>>>>>>>>>>>>>>>>>>>> TWITTER: " + l) )

    //   Some( Result(Image, photos.map(p => <div class="box"><img src={p} /></div>)))
    None
  }

}
