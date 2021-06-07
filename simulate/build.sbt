// name := "simulate"
// 
// scalaVersion := "2.11.12"

// scalacOptions ++= Seq("-deprecation")

// libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"

// commonSourcePackages += "types"

// Global / onChangedBuildSource := ReloadOnSourceChanges

val commonSettings = Seq(
	scalaVersion := "2.12.1",
	organization := "mattjp"	
)

lazy val root = (project in file("."))
	.settings(
		name := "sim"

		// Add additional library dependencies here
	)

// sourceDirectories in Compile += file("/types")
