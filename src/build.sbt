Global / onChangedBuildSource := ReloadOnSourceChanges

val commonSettings = Seq(
	scalaVersion := "2.12.1",
	organization := "mattjp"	
)

lazy val root = (project in file("."))
	.settings(
		name := "sim"

		// Add additional library dependencies here
	)

sourceDirectories in Compile += file("/types")
