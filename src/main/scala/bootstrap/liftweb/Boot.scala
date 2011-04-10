package bootstrap.liftweb

import net.liftweb._,
  util.NamedPF,
  http.{LiftRules, NotFoundAsTemplate, ParsePath, XHtmlInHtml5OutProperties, Req},
  sitemap.{SiteMap, Menu, Loc}

import akka.actor.Actor.actorOf
import akka.actor.Supervisor
import akka.config.Supervision.{SupervisorConfig,OneForOneStrategy,Supervise,Permanent}

class Boot {
  def boot {
    LiftRules.addToPackages("teamawesome")
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    LiftRules.uriNotFound.prepend(NamedPF("404handler"){
      case (req,failure) => NotFoundAsTemplate(
        ParsePath(List("exceptions","404"),"html",false,false))
    })
    
    LiftRules.setSiteMap(SiteMap(
      Menu("Home") / "index"
    ))
    
    LiftRules.htmlProperties.default.set((r: Req) => 
      new XHtmlInHtml5OutProperties(r.userAgent)) 
    
    
    /**
     * Load actors upon booting the application
     */
    Supervisor(
      SupervisorConfig(
        OneForOneStrategy(List(classOf[Throwable]), 3, 1000),
        Supervise(
          actorOf[teamawesome.actor.QueryDispatcher],
          Permanent,
          false) ::
        Supervise(
          actorOf[teamawesome.actor.Stalker],
          Permanent,
          false) ::
        Nil))
    
  }
}
