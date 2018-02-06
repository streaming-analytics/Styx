// enable publishing the jar produced by `test:package`
publishArtifact in (Test, packageBin) := true
// enable publishing the test API jar
publishArtifact in (Test, packageDoc) := true
// enable publishing the test sources jar
publishArtifact in (Test, packageSrc) := true

// if it also needs to be packaged: publishArtifact in (IntegrationTest, packageBin) := true
// if it also needs to be packaged: publishArtifact in (IntegrationTest, packageDoc) := true
// if it also needs to be packaged: publishArtifact in (IntegrationTest, packageSrc) := true

assemblyMergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case m if m.toLowerCase.endsWith(".dct")                 => MergeStrategy.first
  case n if n.endsWith(".conf")                            => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}
