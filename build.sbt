scalaVersion := "2.13.10"
organization := "ronancamargo"
name         := "firecats"
version      := "0.0.1-SNAPSHOT"

val circeVersion         = "0.14.5"
val catsEffectVersion    = "3.4.8"
val catsVersion          = "2.9.0"
val catsMtlVersion       = "1.3.0"
val mouseVersion         = "1.2.1"
val scalaTestVersion     = "3.2.15"
val mockitoScalaVersion  = "1.17.12"
val monocleVersion       = "3.1.0"
val fs2Version           = "3.6.1"
val shapelessVersion     = "2.3.10"
val kindProjectorVersion = "0.13.2"
val magnoliaVersion      = "1.1.3"

addCompilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
libraryDependencies ++= dependencies

lazy val dependencies =
  catsDependencies ++ monocleDependencies ++ circeDependencies ++ testDependencies ++ fs2Dependencies ++ shapelessDependencies ++ firestoreDependencies ++ magnoliaDependencies

lazy val catsDependencies = Seq(
  "org.typelevel" %% "cats-core"   % catsVersion,
  "org.typelevel" %% "cats-mtl"    % catsMtlVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "org.typelevel" %% "mouse"       % mouseVersion
)

lazy val testDependencies    = Seq(
  "org.scalatest" %% "scalatest"     % scalaTestVersion    % Test,
  "org.mockito"   %% "mockito-scala" % mockitoScalaVersion % Test
)
lazy val monocleDependencies = Seq(
  "dev.optics" %% "monocle-core"  % monocleVersion,
  "dev.optics" %% "monocle-law"   % monocleVersion,
  "dev.optics" %% "monocle-macro" % monocleVersion
)

lazy val circeDependencies = Seq(
  "io.circe" %% "circe-core"           % circeVersion,
  "io.circe" %% "circe-generic"        % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-parser"         % circeVersion
)

lazy val fs2Dependencies = Seq(
  "co.fs2" %% "fs2-core"             % fs2Version,
  "co.fs2" %% "fs2-io"               % fs2Version,
  "co.fs2" %% "fs2-reactive-streams" % fs2Version,
  "co.fs2" %% "fs2-scodec"           % fs2Version
)

lazy val shapelessDependencies = Seq(
  "com.chuusai" %% "shapeless" % shapelessVersion
)

lazy val firestoreDependencies = Seq(
  "com.google.firebase" % "firebase-admin"         % "9.1.1",
  "com.google.cloud"    % "google-cloud-firestore" % "3.7.9"
)

lazy val magnoliaDependencies = Seq(
  "com.softwaremill.magnolia1_2" %% "magnolia" % magnoliaVersion
)
