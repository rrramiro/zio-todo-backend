val http4sVersion     = "0.21.24" // cats-effect fs2
val circeVersion      = "0.14.1" // cats-core
val doobieVersion     = "0.13.4" // cats-effect quill
val zioVersion        = "1.0.9"
val zioReactVersion   = "1.3.5"
val zioIzumiVersion   = "1.1.2"
val fs2Version        = "2.5.6" //cats-effect
val silencerVersion   = "1.7.5"
val acyclicVersion    = "0.2.1"
val calibanVersion    = "1.0.0" // cats-core http4s
val sttpVersion       = "3.2.3" // cats-effect circe
val pureconfigVersion = "0.16.0"
val catsVersion       = "2.6.1"
val catsEffectVersion = "2.5.1"
val zioCatsVersion    = "2.4.1.0"
val quillVersion      = "3.7.1" // doobie
val hikariCPVersion   = "3.4.5" //"4.0.1" slf4j 2.0.0-alpha1
val flywayVersion     = "7.10.0"
val h2Version         = "1.4.200"
val slf4jVersion      = "1.7.30"
val sourcecodeVersion = "0.2.7"
val kindProjectorVersion = "0.13.0"
val splainVersion        = "0.5.8"
val bmfVersion           = "0.3.1"
val scalaCollCompatVersion = "2.4.4"

val wartremoverCompileExclusions = Seq(
  Wart.Overloading,
  Wart.PublicInference,
  Wart.Equals,
  Wart.ImplicitParameter,
  Wart.Serializable,
  Wart.JavaSerializable,
  Wart.DefaultArguments,
  Wart.Var,
  Wart.Product,
  Wart.Any,
  Wart.ExplicitImplicitTypes,
  Wart.ImplicitConversion,
  Wart.Nothing,
  Wart.MutableDataStructures
)

val wartremoverTestCompileExclusions = wartremoverCompileExclusions ++ Seq(
  Wart.DefaultArguments,
  Wart.Var,
  Wart.AsInstanceOf,
  Wart.IsInstanceOf,
  Wart.TraversableOps,
  Wart.Option2Iterable,
  Wart.JavaSerializable,
  Wart.FinalCaseClass,
  Wart.NonUnitStatements
)

val filterConsoleScalacOptions = { options: Seq[String] =>
  options.filterNot(
    Set(
      "-Ywarn-unused:imports",
      "-Ywarn-unused-import",
      "-Ywarn-dead-code",
      "-Xfatal-warnings"
    )
  )
}

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "check",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)
addCommandAlias(
  "gengraphql",
  "calibanGenClient project/schema.graphql src/main/scala/com/schuwalow/zio/todo/graphql/Client.scala"
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin, CodegenPlugin)
  .settings(
    Docker / packageName := "zio-todo",
    Docker / dockerUsername := Some("todouser"),
    Docker / dockerExposedPorts := Seq(8080),
    organization := "com.schuwalow",
    name := "zio-todo-backend",
    maintainer := "maxim.schuwalow@gmail.com",
    licenses := Seq(
      "MIT" -> url(
        s"https://github.com/mschuwalow/${name.value}/blob/v${version.value}/LICENSE"
      )
    ),
    scalaVersion := "2.13.5",
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    scalacOptions := Seq(
      "-feature",
      "-deprecation",
      "-explaintypes",
      "-unchecked",
      "-encoding",
      "UTF-8",
      "-language:higherKinds",
      "-language:existentials",
      "-Xfatal-warnings",
      "-Xlint:-infer-any,-byname-implicit,_",
      //"-Xlog-implicits",
      "-Ywarn-value-discard",
      "-Ywarn-numeric-widen",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused:_",
      "-Ymacro-annotations"
    ) ++ (if (isSnapshot.value) Seq.empty
          else
            Seq(
              "-opt:l:inline"
            )) ++ Seq(
      "-P:acyclic:force",
      //"-P:splain:all", //TODO comment to have the macros "zio.macros.annotation.accessible" working
      "-P:silencer:checkUnused"
    ),
    dependencyCheckCveUrlModified := Some(
      new URL("http://nvdmirror.sml.io/")
    ),
    dependencyCheckCveUrlBase := Some("http://nvdmirror.sml.io/"),
    dependencyCheckAssemblyAnalyzerEnabled := Some(false),
    dependencyCheckFormat := "All",
    Compile / compile / wartremoverWarnings := Warts.all
      .diff(wartremoverCompileExclusions),
    Test / compile / wartremoverWarnings := Warts.all
      .diff(wartremoverTestCompileExclusions),
    Compile / console / scalacOptions ~= filterConsoleScalacOptions,
    Test / console / scalacOptions ~= filterConsoleScalacOptions,
    libraryDependencies ++= Seq(
      "org.http4s"                   %% "http4s-blaze-server"         % http4sVersion,
      "org.http4s"                   %% "http4s-dsl"                  % http4sVersion,
      "org.http4s"                   %% "http4s-circe"                % http4sVersion,
      "org.http4s"                   %% "http4s-core"                 % http4sVersion,
      "org.http4s"                   %% "http4s-server"               % http4sVersion,
      "io.circe"                     %% "circe-core"                  % circeVersion,
      "io.circe"                     %% "circe-generic"               % circeVersion,
      //"io.circe"                     %% "circe-optics"                % circeVersion,
      "io.circe"                     %% "circe-literal"               % circeVersion % Test,
      "com.github.ghostdogpr"        %% "caliban"                     % calibanVersion,
      "com.github.ghostdogpr"        %% "caliban-http4s"              % calibanVersion,
      "com.github.ghostdogpr"        %% "caliban-client"              % calibanVersion,
      "com.github.ghostdogpr"        %% "caliban-federation"          % calibanVersion,
      "org.tpolecat"                 %% "doobie-core"                 % doobieVersion,
      "org.tpolecat"                 %% "doobie-free"                 % doobieVersion,
      "org.tpolecat"                 %% "doobie-h2"                   % doobieVersion,
      "org.tpolecat"                 %% "doobie-hikari"               % doobieVersion,
      "org.tpolecat"                 %% "doobie-quill"                % doobieVersion,
      "com.softwaremill.sttp.client3" %% "core"                       % sttpVersion % Test,
      "com.softwaremill.sttp.client3" %% "http4s-backend"             % sttpVersion % Test,
      "dev.zio"                      %% "zio"                         % zioVersion,
      "dev.zio"                      %% "zio-streams"                 % zioVersion,
      "dev.zio"                      %% "zio-test"                    % zioVersion % Test,
      "dev.zio"                      %% "zio-test-sbt"                % zioVersion % Test,
      "dev.zio"                      %% "zio-interop-cats"            % zioCatsVersion,
      "dev.zio"                      %% "zio-interop-reactivestreams" % zioReactVersion,
      "dev.zio"                      %% "izumi-reflect"               % zioIzumiVersion,
      "co.fs2"                       %% "fs2-core"                    % fs2Version,
      "co.fs2"                       %% "fs2-reactive-streams"        % fs2Version,
      "com.github.pureconfig"        %% "pureconfig"                  % pureconfigVersion,
      "com.github.pureconfig"        %% "pureconfig-core"             % pureconfigVersion,
      "com.github.pureconfig"        %% "pureconfig-generic"          % pureconfigVersion,
      "org.typelevel"                %% "cats-core"                   % catsVersion,
      "org.typelevel"                %% "cats-free"                   % catsVersion,
      "org.typelevel"                %% "cats-effect"                 % catsEffectVersion,
      "io.getquill"                  %% "quill-core"                  % quillVersion,
      "io.getquill"                  %% "quill-jdbc"                  % quillVersion,
      "io.getquill"                  %% "quill-sql"                   % quillVersion,
      "com.zaxxer"                   % "HikariCP"                     % hikariCPVersion,
      "org.flywaydb"                 % "flyway-core"                  % flywayVersion,
      "com.h2database"               % "h2"                           % h2Version,
      "org.slf4j"                    % "slf4j-api"                    % slf4jVersion,
      "org.slf4j"                    % "slf4j-log4j12"                % slf4jVersion,
      "com.lihaoyi"                  %% "sourcecode"                  % sourcecodeVersion,
      "com.lihaoyi"                  %% "acyclic"                     % acyclicVersion % "provided",
      "org.scala-lang.modules"       %% "scala-collection-compat"     % scalaCollCompatVersion,
      ("com.github.ghik" % "silencer-lib" % silencerVersion % "provided")
        .cross(CrossVersion.full),
      // plugins
      compilerPlugin("com.lihaoyi" %% "acyclic" % acyclicVersion),
      compilerPlugin(
        ("io.tryp" % "splain" % splainVersion).cross(CrossVersion.patch)
      ), //TODO comment to have the macros "zio.macros.annotation.accessible" working
      compilerPlugin("com.olegpy" %% "better-monadic-for" % bmfVersion),
      compilerPlugin(
        ("org.typelevel" % "kind-projector" % kindProjectorVersion).cross(CrossVersion.full)
      ),
      compilerPlugin(
        ("com.github.ghik" % "silencer-plugin" % silencerVersion)
          .cross(CrossVersion.full)
      )
    )
  )

//release
/*
import ReleaseTransformations._
import ReleasePlugin.autoImport._
import sbtrelease.{ Git, Utilities }
import Utilities._

releaseProcess := Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  pushChanges,
  tagRelease,
  mergeReleaseVersion,
  ReleaseStep(releaseStepTask(publish in Docker)),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
*/
missinglinkIgnoreDestinationPackages ++= Seq(
  IgnoredPackage("play.api.libs"), //caliban.interop.play
  IgnoredPackage("zio.json") // caliban.interop.zio
)
missinglinkIgnoreSourcePackages ++= Seq(
  IgnoredPackage("org.flywaydb.core"),
  IgnoredPackage("com.zaxxer.hikari.metrics")
)
//missinglinkExcludedDependencies += moduleFilter(organization = "ch.qos.logback", name = "logback-core")
/*
val mergeBranch = "master"

val mergeReleaseVersion = ReleaseStep(action = st => {
  val git       = st.extract.get(releaseVcs).get.asInstanceOf[Git]
  val curBranch = (git.cmd("rev-parse", "--abbrev-ref", "HEAD") !!).trim
  st.log.info(s"####### current branch: $curBranch")
  git.cmd("checkout", mergeBranch) ! st.log
  st.log.info(s"####### pull $mergeBranch")
  git.cmd("pull") ! st.log
  st.log.info(s"####### merge")
  git.cmd("merge", curBranch, "--no-ff", "--no-edit") ! st.log
  st.log.info(s"####### push")
  git.cmd("push", "origin", s"$mergeBranch:$mergeBranch") ! st.log
  st.log.info(s"####### checkout $curBranch")
  git.cmd("checkout", curBranch) ! st.log
  st
})
*/
Global / onChangedBuildSource := ReloadOnSourceChanges
