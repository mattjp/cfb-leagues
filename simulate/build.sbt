val commonSettings = Seq(
	scalaVersion := "2.13.6",
	organization := "mattjp"	
)

lazy val root = (project in file("."))
	.settings(
		name := "simulate",
		libraryDependencies += "com.lihaoyi"     %% "requests"      % "0.6.5",
		libraryDependencies += "com.lihaoyi"     %% "upickle"       % "1.3.8",
		// libraryDependencies += "io.github.d2a4u" %% "meteor-awssdk" % "1.0.6",

		// libraryDependencies += "io.lemonlabs"       %% "scala-uri" % "3.5.0",
		// libraryDependencies += "org.scanamo" %% "scanamo" % "1.0.0-M15"
		libraryDependencies += "com.github.seratch" %% "awscala-dynamodb"   % "0.8.5"
	)
