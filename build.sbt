val http4sVersion     = "0.21.12"
val circeVersion      = "0.13.0"
val doobieVersion     = "0.9.4"
val zioVersion        = "1.0.3"
val zioCatsVersion    = "2.1.4.0"
val zioReactVersion   = "1.0.3.5"
val zioIzumiVersion   = "1.0.0-M7"
val fs2Version        = "2.4.4"
val silencerVersion   = "1.7.1"
val acyclicVersion    = "0.2.0"
val calibanVersion    = "0.9.3"
val sttpVersion       = "2.2.9"
val pureconfigVersion = "0.14.0"
val catsVersion       = "2.2.0"
val catsEffectVersion = "2.2.0"
val quillVersion      = "3.5.3"
val hikariCPVersion   = "3.4.5"
val flywayVersion     = "7.3.0"
val h2Version         = "1.4.200"
val slf4jVersion      = "1.7.30"
val sourcecodeVersion = "0.2.1"

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
    packageName in Docker := "zio-todo",
    dockerUsername in Docker := Some("mnt"),
    dockerExposedPorts in Docker := Seq(8080),
    organization := "com.schuwalow",
    name := "zio-todo-backend",
    maintainer := "maxim.schuwalow@gmail.com",
    licenses := Seq(
      "MIT" -> url(
        s"https://github.com/mschuwalow/${name.value}/blob/v${version.value}/LICENSE"
      )
    ),
    scalaVersion := "2.13.4",
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
    wartremoverWarnings in (Compile, compile) := Warts.all
      .diff(wartremoverCompileExclusions),
    wartremoverWarnings in (Test, compile) := Warts.all
      .diff(wartremoverTestCompileExclusions),
    scalacOptions in (Compile, console) ~= filterConsoleScalacOptions,
    scalacOptions in (Test, console) ~= filterConsoleScalacOptions,
    libraryDependencies ++= Seq(
      "org.http4s"                   %% "http4s-blaze-server"         % http4sVersion,
      "org.http4s"                   %% "http4s-dsl"                  % http4sVersion,
      "org.http4s"                   %% "http4s-circe"                % http4sVersion,
      "org.http4s"                   %% "http4s-core"                 % http4sVersion,
      "org.http4s"                   %% "http4s-server"               % http4sVersion,
      "io.circe"                     %% "circe-core"                  % circeVersion,
      "io.circe"                     %% "circe-generic"               % circeVersion,
      "io.circe"                     %% "circe-optics"                % circeVersion,
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
      "com.softwaremill.sttp.client" %% "core"                        % sttpVersion,
      "com.softwaremill.sttp.client" %% "http4s-backend"              % sttpVersion % Test,
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
      ("com.github.ghik" % "silencer-lib" % silencerVersion % "provided")
        .cross(CrossVersion.full),
      // plugins
      compilerPlugin("com.lihaoyi" %% "acyclic" % acyclicVersion),
      compilerPlugin(
        ("io.tryp" % "splain" % "0.5.7").cross(CrossVersion.patch)
      ), //TODO comment to have the macros "zio.macros.annotation.accessible" working
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
      compilerPlugin(
        ("org.typelevel" % "kind-projector" % "0.11.1").cross(CrossVersion.full)
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
  IgnoredPackage("play.api.libs")
)
missinglinkIgnoreSourcePackages ++= Seq(
  IgnoredPackage("org.flywaydb.core"),
  IgnoredPackage("com.zaxxer.hikari")
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
