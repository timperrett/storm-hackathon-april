package teamawesome.actor

import net.liftweb._,
  http.CometActor
import akka.actor.{Actor,ActorRef}

trait AkkaCometActor extends CometActor {
  implicit val akkaProxy: Option[ActorRef] = Some(Actor.actorOf(new Actor{
    protected def receive = {
      case a => AkkaCometActor.this ! a
    }
  }))
  override def localSetup {
    super.localSetup
    akkaProxy.foreach(_.start)
  }
  override def localShutdown {
    super.localShutdown
    akkaProxy.foreach(_.stop)
  }
}

