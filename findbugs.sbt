findbugsAnalyzedPath := List(
  baseDirectory.value / "styx-app" / "target" / "classes",
  baseDirectory.value / "styx-appRunner" / "target" / "classes",
  baseDirectory.value / "styx-commons" / "target" / "classes",
  baseDirectory.value / "styx-domain" / "target" / "classes",
  baseDirectory.value / "styx-interfaces" / "target" / "classes",
  baseDirectory.value / "styx-flink-kafka" / "target" / "classes",
  baseDirectory.value / "styx-frameworks-flink" / "target" / "classes",
  baseDirectory.value / "styx-frameworks-kafka" / "target" / "classes",
  baseDirectory.value / "styx-frameworks-cassandra" / "target" / "classes",
  baseDirectory.value / "styx-frameworks-openscoring" / "target" / "classes",
  baseDirectory.value / "styx-support-datagen" / "target" / "classes"
)

libraryDependencies += "com.mebigfatguy.fb-contrib" % "fb-contrib" % "7.2.0"
libraryDependencies += "com.h3xstream.findsecbugs" % "findsecbugs-plugin" % "1.4.6"

findbugsPluginList += s"${ivyPaths.value.ivyHome.get.absolutePath}/cache/com.mebigfatguy.fb-contrib/fb-contrib/jars/fb-contrib-7.2.0.jar"
findbugsPluginList += s"${ivyPaths.value.ivyHome.get.absolutePath}/cache/com.h3xstream.findsecbugs/findsecbugs-plugin/jars/findsecbugs-plugin-1.4.6.jar"
findbugsEffort := FindBugsEffort.Maximum
findbugsExcludeFilters := Some(scala.xml.XML.loadFile(baseDirectory.value / "findbugs-excludefilter.xml"))
findbugsReportPath := Some(file("target/findbugs/report.xml"))

