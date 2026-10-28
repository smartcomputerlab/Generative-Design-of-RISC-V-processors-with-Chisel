scalaVersion := "2.12.17"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:reflectiveCalls",
)

// Chisel 3.5.6
val chiselVersion = "3.5.6"

// IMPORTANT: Add the compiler plugin
addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % chiselVersion cross CrossVersion.full)

libraryDependencies ++= Seq(
  "edu.berkeley.cs" %% "chisel3" % chiselVersion,
  "edu.berkeley.cs" %% "chiseltest" % "0.5.6" % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)

