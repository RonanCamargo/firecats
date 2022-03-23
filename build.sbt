scalaVersion := "2.13.8"
organization := "ronancam"
name         := "firecats"

val circeVersion         = "0.14.1"
val catsEffectVersion    = "3.3.5"
val catsVersion          = "2.7.0"
val catsMtlVersion       = "1.2.1"
val mouseVersion         = "1.0.9"
val scalaTestVersion     = "3.2.10"
val mockitoScalaVersion  = "1.17.0"
val monocleVersion       = "3.1.0"
val fs2Version           = "3.2.4"
val shapelessVersion     = "2.3.8"
val kindProjectorVersion = "0.13.2"

addCompilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.full)
libraryDependencies ++= dependencies

lazy val dependencies =
  catsDependencies ++ monocleDependencies ++ circeDependencies ++ testDependencies ++ fs2Dependencies ++ shapelessDependencies ++ firestoreDependencies

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
  "com.google.firebase" % "firebase-admin"         % "8.1.0",
  "com.google.cloud"    % "google-cloud-firestore" % "3.0.14"
)
