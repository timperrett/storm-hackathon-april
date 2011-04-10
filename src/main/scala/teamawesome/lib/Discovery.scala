package teamawesome.lib

import scala.xml.NodeSeq

// sealed trait Presentation {
//   def data: T
//   def display: NodeSeq 
// }
// 
// trait TextualPresentation extends Presentation {
//   def display = Text()
// }

case class Fact[T](data: T)
case class Discovered(items: List[Fact[_]])
