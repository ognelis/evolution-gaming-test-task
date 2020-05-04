
lazy val maqexoju = (project in file("maqexoju"))
  .enablePlugins(DockerPlugin)
  .settings(
    scalaVersion := "2.12.10",
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-Ypartial-unification",
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    libraryDependencies ++= Dependencies.maqexoju,
    dockerfile in docker := {
      // The assembly task generates a fat JAR file
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("openjdk:8-jre")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
        expose(9000)
      }
    }
  )
