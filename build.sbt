name := "ctp-gsheets4s"
organization := "com.itv"

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
  scalaVersion := "2.12.6",
)

lazy val catsVersion = "2.3.1"
lazy val catsEffectVersion = "2.3.1"
lazy val circeVersion = "0.13.0"
lazy val refinedVersion = "0.9.20"
lazy val attoVersion = "0.9.0"
lazy val hammockVersion = "0.11.3"
lazy val scalacheckVersion = "1.15.2"
lazy val scalatestVersion = "3.2.3"
lazy val scalaUriVersion = "3.0.0"

lazy val gsheets4s = project.in(file("."))
  .settings(name := "gsheets4s")
  .settings(baseSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffectVersion,
      "eu.timepit" %% "refined" % refinedVersion,
      "io.lemonlabs" %% "scala-uri" % scalaUriVersion,
    ) ++ Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion) ++ Seq(
      "org.tpolecat" %% "atto-core",
      "org.tpolecat" %% "atto-refined"
    ).map(_ % attoVersion) ++ Seq(
      "com.pepegar" %% "hammock-core",
      "com.pepegar" %% "hammock-apache-http",
      "com.pepegar" %% "hammock-circe"
    ).map(_ % hammockVersion) ++ Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion,
      "org.scalacheck" %% "scalacheck" % scalacheckVersion,
      "eu.timepit" %% "refined-scalacheck" % refinedVersion
    ).map(_ % "test")
  )


resolvers += "Artifactory Realm" at "https://itvrepos.jfrog.io/itvrepos/fp-scala-libs/"

credentials += Credentials(Path.userHome / ".ivy2" / "fp-scala-libs.credentials")

publishArtifact := true
publishArtifact in Test := false
publishTo := Some("Artifactory Realm" at "https://itvrepos.jfrog.io/itvrepos/fp-scala-libs/")
