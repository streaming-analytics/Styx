//assemblyJarName := "desired_jar_name_after_assembly.jar"

val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.startsWith("application.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case meta(_) => MergeStrategy.discard
  case _ => MergeStrategy.first
}
