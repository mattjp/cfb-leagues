val commonSettings = Seq(
	scalaVersion := "2.12.1",
	organization := "mattjp"	
)

lazy val root = (project in file("."))
	.settings(
		name := "simulate",
		libraryDependencies += "com.lihaoyi"  %% "requests"  % "0.6.5",
		libraryDependencies += "com.lihaoyi"  %% "upickle"   % "1.3.8",
		libraryDependencies += "io.lemonlabs" %% "scala-uri" % "3.5.0"
		// libraryDependencies += "org.scalaj"  %% "scalaj-http" % "2.4.2",
		// libraryDependencies += "io.gatling"  %% "gatling-sbt" % "3.6.0"
	)
