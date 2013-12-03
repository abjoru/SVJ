import sbt._
import Keys._

object SVLBuild extends Build {
	import Resolvers._
	import Dependencies._
	import BuildSettings._

	override lazy val settings = super.settings ++ globalSettings

	lazy val root = Project("SVJ",
		file("."),
		settings = projectSettings
	) aggregate (opencvTest, jviolajonesTest, svj, datasets)

	lazy val svj = Project("svj",
		file("svj"),
		settings = projectSettings ++ Seq(
			scalacOptions ++= Seq("-optimise", "-feature"),
			autoCompilerPlugins := true,
			libraryDependencies ++= Seq(javacl, scalacl, akka, akkaCluster, slf4jSimple)
		)
	) dependsOn(datasets % "compile->test")

	lazy val opencvTest = Project("opencv-test",
		file("opencv-test"),
		settings = projectSettings
	) dependsOn(datasets)

	lazy val jviolajonesTest = Project("jviolajones-test",
		file("jviolajones-test"),
		settings = projectSettings ++ Seq(
			libraryDependencies ++= Seq(jviolajones)
		)
	) dependsOn(datasets)

	lazy val datasets = Project("datasets",
		file("datasets"),
		settings = projectSettings ++ Seq(
			libraryDependencies ++= Seq(scalachart)
		)
	)
}

object BuildSettings {
	import Resolvers._
	import Dependencies._

	val buildOrganization = "edu.fit.cs"
	val buildVersion = "0.1"
	val buildScalaVersion = "2.10.3"
	
	val globalSettings = Seq(
		organization := buildOrganization,
		version := buildVersion,
		scalaVersion := buildScalaVersion,
		scalacOptions += "-deprecation",
		fork in run := true,
		fork in test := true,
		libraryDependencies ++= Seq(slf4jSimpleTest, scalatest, scalameter, junit),
		resolvers := Seq(nativeLibsRepo, scalaToolsRepo, akkaRepo, sonatypeRepo, sonatypeSnap, localRepo),
		testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
		logBuffered := false
	)

	val projectSettings = Defaults.defaultSettings ++ globalSettings
}

object Resolvers {
	val sonatypeRepo = "Sonatype Release" at "http://oss.sonatype.org/content/repositories/releases"
	val sonatypeSnap = "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"
	val nativeLibsRepo = "NativeLibs4Java" at "http://nativelibs4java.sourceforge.net/maven/"
	val scalaToolsRepo = "Scala Tools" at "http://scala-tools.org/repo-snapshots"
	val akkaRepo = "Akka" at "http://akka.io/repository/"
	val localRepo = "LocalMaven" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
}

object Dependencies {
	val scalatest = "org.scalatest" %% "scalatest" % "1.9.2" % "test"
	val scalameter = "com.github.axel22" %% "scalameter" % "0.4" % "test"
	val slf4jSimple = "org.slf4j" % "slf4j-simple" % "1.7.5"
	val slf4jSimpleTest = slf4jSimple % "test"
	val scalachart = "com.github.wookietreiber" %% "scala-chart" % "latest.integration"
	val akka = "com.typesafe.akka" %% "akka-actor" % "2.3-M1"
	val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % "2.3-M1"
	val jviolajones = "jviolajones" % "jviolajones" % "1.0.2"
	val junit = "junit" % "junit" % "4.+" % "test"
	val javacl = "com.nativelibs4java" % "javacl" % "1.0.0-RC2"
	val scalacl = "com.nativelibs4java" % "scalacl" % "0.2"
}
