package teamawesome.lib

import scala.xml.{NodeSeq,Text}

sealed trait Presentable {
  def toDisplay: NodeSeq 
}

case class Fact[T <: Presentable](data: T)

case class Website(uri: String) extends Presentable {
  def toDisplay = <a href={uri} target="_blank">{uri}</a>
}

case class Photo(uri: String) extends Presentable {
  def toDisplay = <img src={uri} alt="" />
}

case class Discovered[T <: Presentable](items: List[Fact[T]])
