

name := "generators"
ThisBuild / organization := "de.ekut.tbi"
ThisBuild / version := "1.0-SNAPSHOT"

lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.12"
lazy val supportedScalaVersions =
  List(
    scala212,
    scala213
  )

ThisBuild / scalaVersion := scala213

unmanagedSourceDirectories in Compile += {
  val sourceDir = (sourceDirectory in Compile).value
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
    case _                       => sourceDir / "scala-2.13-"
  }
}


//-----------------------------------------------------------------------------
// PROJECT
//-----------------------------------------------------------------------------

lazy val root = project.in(file("."))
  .settings(settings)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai"   %% "shapeless" % "2.3.3",
      "org.typelevel" %% "cats-core" % "2.1.1",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
   ),
   crossScalaVersions := supportedScalaVersions
 )


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings

lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-Xfatal-warnings",
  "-feature",
  "-language:higherKinds",
  "-deprecation"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
  
)

