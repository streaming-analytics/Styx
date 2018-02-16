addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.1.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.2") // enables 'sbt assembly' to create a fat jar
addSbtPlugin("uk.co.josephearl" % "sbt-findbugs" % "2.4.3") // enables 'sbt findbugs'
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.4") // enables 'sbt coverage test'
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4") //enables 'sbt scapegoat' (scala checkstyle)
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0") //enables 'sbt dependencyTree' (transitive dependencies)
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.4") // enables 'sbt dependencyUpdates'
