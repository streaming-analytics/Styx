resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe Simple Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/"
resolvers += Resolver.bintrayRepo("hseeberger", "maven")
resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.3")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.4.2")
addSbtPlugin("net.vonbuchholtz" % "sbt-dependency-check" % "1.1.0")
addSbtPlugin("org.jmotor.sbt" % "sbt-dependency-updates" % "1.2.1")
