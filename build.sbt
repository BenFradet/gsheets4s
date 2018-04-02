organization in ThisBuild := "com.github.benfradet"

lazy val compilerOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-unchecked",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused-import",
  "-Xfuture",
  "-Ypartial-unification"
)

lazy val baseSettings = Seq(
  scalacOptions ++= compilerOptions,
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  scalacOptions in (Test, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  scalaVersion := "2.12.5",
  version := "0.1.0-SNAPSHOT"
)

lazy val catsVersion = "1.1.0"
lazy val scalatestVersion = "3.0.5"

lazy val gsheets4s = project.in(file("."))
  .settings(name := "gsheets4s")
  .settings(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion
    ) ++ Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion
    ).map(_ % "test")
  )
