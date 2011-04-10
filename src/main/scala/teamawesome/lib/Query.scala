package teamawesome.lib

case class Query(content: String)

object QueryIdentifier extends Enumeration {
  val TwitterUsername, EmailAddress, Website = Value
}

trait QueryAugmentation {
  import QueryIdentifier._
  type InputAssement = String => Option[QueryIdentifier.Value]
  
  lazy val IsTwitterUsername: InputAssement = x => x.toSeq match {
    case Seq('@', _*) => Some(TwitterUsername)
    case _ => None
  }
  
  lazy val IsEmailAddress: InputAssement =  
    "^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$".r.findFirstIn(_).map(x => EmailAddress)
  
  lazy val IsWebsite: InputAssement =  
    "^http://".r.findFirstIn(_).map(x => Website)
}