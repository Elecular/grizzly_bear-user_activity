lazy val app = (project in file(".")).
    settings(
        name := "User Activity Processor",
        version := "1.0",
        scalaVersion := "2.11.7",
        assemblyJarName in assembly := "build.jar",
        libraryDependencies ++= Seq(
            "org.mongodb.spark" %% "mongo-spark-connector" % "2.4.1",
            "org.apache.spark" %% "spark-core" % "2.4.1" % "provided",
            "org.apache.spark" %% "spark-sql" % "2.4.1" % "provided"
        )
    )

lazy val local = project.in(file("local")).dependsOn(RootProject(file("."))).settings(
    name := "Dev User Activity Processor",
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(
        "org.mongodb.spark" %% "mongo-spark-connector" % "2.4.1",
        "org.apache.spark" %% "spark-core" % "2.4.1",
        "org.apache.spark" %% "spark-sql" % "2.4.1"
    ),
    mainClass in (Compile,run) := Some("UserActivityProcessor")
)