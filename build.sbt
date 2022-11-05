organization := "me.jeffshaw.tryutils"

name := "tryutils"

version := "1.0.2-SNAPSHOT"

versionScheme := Some("semver-spec")

scalaVersion := "3.2.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.14" % Test,
  "org.scalatestplus" %% "testng-7-5" % "3.2.14.0" % Test
)

crossScalaVersions := Seq(
  "2.13.10",
  "2.12.17",
  "2.11.12"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, _)) =>
      Seq("-target:jvm-1.8")
    case Some((3, _)) =>
      Seq("-release", "8")
    case _ =>
      throw new RuntimeException("unknown Scala version " + scalaVersion.value)
  }
}

resolvers ++= Resolver.sonatypeOssRepos("releases")

mimaPreviousArtifacts :=
  Set(
    "me.jeffshaw.tryutils" %% "tryutils" % "1.0.0",
    "me.jeffshaw.tryutils" %% "tryutils" % "1.0.1"
  )

licenses := Seq("Apache License, Version 2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/shawjef3/tryutils"))

scmInfo := Some(ScmInfo(url("https://github.com/shawjef/tryutils"), "git@github.com:shawjef3/tryutils.git"))

developers := List(Developer("shawjef3", "Jeff Shaw", "", url("https://www.github.com/shawjef3")))

publishMavenStyle := true
