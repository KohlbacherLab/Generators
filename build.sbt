

name := "generators"
organization in ThisBuild := "de.ekut.tbi"
scalaVersion in ThisBuild := "2.12.8"
//scalaVersion in ThisBuild := "2.13.0"
version := "0.1-SNAPSHOT"



//-----------------------------------------------------------------------------
// PROJECTS
//-----------------------------------------------------------------------------


lazy val root = project.in(file("."))
  .settings(settings)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % "2.3.3",
      "org.typelevel" %% "cats-core" % "2.0.0-M4",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test"
//      "org.scalatest" % "scalatest_2.13" % "3.0.8" % "test"
   ),
 )


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings


lazy val compilerOptions = Seq(
  "-unchecked",
//  "-feature",
//  "-language:existentials",
//  "-language:higherKinds",
//  "-language:implicitConversions",
//  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)

