package teamawesome.lib

import scala.xml.NodeSeq


// trait MediaType
// case object Text extends MediaType
// case object Video extends MediaType
// case object Image extends MediaType

sealed trait Presentation {
  def display: NodeSeq 
}

case class Fact[T <: Presentation](data: T)
case class Discovered(items: List[Fact[_]])
