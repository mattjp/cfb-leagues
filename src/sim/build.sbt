val commonSettings = Seq(
	scalaVersion := "2.12.1",
	organization := "mattjp"	
)

lazy val simulate = (project in file("."))
	.settings(
		name := "simulate"

		// Add additional library dependencies here
	)
