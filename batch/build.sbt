lazy val app = (project in file(".")).
    settings(
        name := "User Activity Processor",
        version := "1.0",
        scalaVersion := "2.11.12",
        assemblyJarName in assembly := "build.jar",
        libraryDependencies ++= Seq(
            "org.mongodb.spark" %% "mongo-spark-connector" % "2.4.1",
            "org.apache.spark" %% "spark-core" % "2.4.5" % "provided",
            "org.apache.spark" %% "spark-sql" % "2.4.5" % "provided"
        )
    )

lazy val local = project.in(file("local")).dependsOn(RootProject(file("."))).settings(
    name := "Dev User Activity Processor",
    version := "1.0",
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
        "org.mongodb.spark" %% "mongo-spark-connector" % "2.4.1",
        "org.apache.spark" %% "spark-core" % "2.4.5",
        "org.apache.spark" %% "spark-sql" % "2.4.5"
    ),
    mainClass in (Compile,run) := Some("UserActivityProcessor")
)