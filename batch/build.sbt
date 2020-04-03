name := "User Activity Processor"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.mongodb.spark" %% "mongo-spark-connector" % "2.4.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.1" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.1" % "provided"

assemblyJarName in assembly := "fat-build.jar"
artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  "thin-build.jar"
}
