package bootstrap.liftweb

import net.liftweb._,
  util.NamedPF,
  http.{LiftRules, NotFoundAsTemplate, ParsePath},
  sitemap.{SiteMap, Menu, Loc}

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
  }
}
