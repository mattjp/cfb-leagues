val commonSettings = Seq(
	scalaVersion := "2.12.10",
	organization := "mattjp"	
)

lazy val root = (project in file("."))
	.settings(
		name := "simulate",
		libraryDependencies += "com.lihaoyi"        %% "requests" % "0.6.5",
		libraryDependencies += "com.lihaoyi"        %% "upickle"  % "1.3.8",
		libraryDependencies += "com.github.seratch" %% "awscala"  % "0.8.5"
	)
