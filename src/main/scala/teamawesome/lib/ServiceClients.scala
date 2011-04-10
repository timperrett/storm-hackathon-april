package teamawesome.lib

import scala.xml.NodeSeq
import dispatch._, Http._, json.Js._, twitter._
import org.apache.commons.net.whois.WhoisClient
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonAST._
import net.liftweb.util.PCDataXmlParser
import org.jsoup.Jsoup

object ServiceClient {
  type ⊛ = Query => Option[Discovered[_]]

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
      println(">>>>>>>>>>>>>>>>>> MAP: " + "http://maps.google.com/maps/api/staticmap?center=" + a.replaceAll("\n", ", ") + "&zoom=16&size=512x256&maptype=roadmap&sensor=false")
    }
    None
  }

  def withContentFrom[T](url: String)(f: String => T): T = new Http().apply(new Request(url) >- f)

  val Twitter: ⊛ = q => {
    def removeSpan(l: String): String = l.replaceAll(".*\">", "").replaceAll("<.*", "")
    def getHref(l: String): String = l.replaceAll(".*href=\"", "").replaceAll("\".*", "")

    val result = withContentFrom("http://twitter.com/" + q.content.substring(1)) { html =>
      html.split("\n").filter(_.toLowerCase.contains("<span class=\"label\">name</span>")).foreach(l => println(">>>>>>>>>>>>>>> FULL NAME: " + removeSpan(l)))
      html.split("\n").filter(_.toLowerCase.contains("<span class=\"label\">bio</span>")).foreach(l => println(">>>>>>>>>>>>>>> BIO: " + removeSpan(l)))
      html.split("\n").filter(_.toLowerCase.contains("<span class=\"label\">web</span>")).foreach(l => println(">>>>>>>>>>>>>>> URL: " + getHref(l)))
      html.split("\n").filter(_.toLowerCase.contains("<span class=\"label\">location</span>")).foreach(l => println(">>>>>>>>>>>>>>> LOCATION: " + removeSpan(l)))
      html.split("\n").filter(_.toLowerCase.contains("<span class=\"label\">location</span>")).foreach(l => println(">>>>>>>>>>>>>>> MAP: " + removeSpan(l).replaceAll("\n", ", ") + "&zoom=16&size=512x256&maptype=roadmap&sensor=false"))
    }

    None
  }

  val AddressAndPhoneFromNameAndLocation: ⊛ = q =>
    {
      val qs = q.content.split('|')
      
      val surname = qs(0).split(' ').last
      val location = qs(1).split(",").head
      
      val url = "http://www.thephonebook.bt.com/publisha.content/en/search/residential/search.publisha?Surname=" + surname + "&Location=" + location
      val doc = Jsoup.connect( url ).get().toString

      val xml = PCDataXmlParser.apply( doc )
      
      xml.foreach { n => 
        val output = (for {
          div <- (n \\ "div")
          rb <- div if (div \ "@class").text == "recordBody"
          rtd <- (rb \ "div")
          rt <- rtd if (((rtd \ "@class").text == "recordTitle") && ( rtd.text.trim.charAt(0) == qs(0)	.charAt(0) )) 
        } yield rb)
        
        output.foreach { rb =>
      		val finalout = (for {
      			spans <- (rb \\ "span")
      			phone <- spans if ( spans \ "@class" ).text == "phone"
  				address <- (rb \ "div")(2)
      		} yield (phone.text, address.text.split('-')(0).trim))
      		
      		
      		finalout.foreach( f => println (f) )
        }
      }
      
      None
    }

  val WhoisFromEmail: ⊛ = q =>
    WhoIs(q.copy(content = q.content.split("@").last))

  private def socialJsonFor(q: Query) =
    Http(
      :/("socialgraph.googleapis.com") / "otherme" <<?
        Map("q" -> q.content) >- JsonParser.parse)

  private def getFacts[A <: Presentable](q: Query, field: String)(func: String => Fact[A]) =
    try {
      Some(Discovered(for {
        JObject(other) <- socialJsonFor(q)
        JField(_, JObject(profile)) <- other
        JField(field, JString(f)) <- profile
      } yield func(f)))
    } catch {
      case e => None
    }

  val URLsFromEmail: ⊛ = q => getFacts[Website](q, "url"){ data => 
    Fact(Website(data))
  }

  val PhotosFromEmail: ⊛ = q => getFacts[Photo](q, "photo"){ data => 
    Fact(Photo(data))
  }

}
