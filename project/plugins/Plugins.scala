import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  lazy val eclipse = "de.element34" % "sbt-eclipsify" % "0.7.0"
  lazy val stax = "eu.getintheloop" % "sbt-stax-plugin" % "0.1.2"
  // repos
  lazy val staxReleases = "stax-release-repo" at "http://mvn.stax.net/content/repositories/public"
}
