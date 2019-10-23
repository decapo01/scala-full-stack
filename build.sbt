import sbtcrossproject.{crossProject, CrossType}

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(client,mformapp,homeapp),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    guice,
    "com.typesafe.play" %% "play-slick" % "3.0.1",
    "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
    "com.h2database" % "h2" % "1.4.192",
    "com.adrianhurt" %% "play-bootstrap" % "1.5.1-P26-B3",
    "org.webjars" %% "webjars-play" % "2.6.3",
    "org.webjars" % "bootstrap" % "3.3.5",
    "org.webjars" % "jquery" % "3.4.1",
    "org.webjars" % "bootstrap-select" % "1.13.8",
    "org.mindrot" % "jbcrypt" % "0.4",
    specs2 % Test
  ),
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val commonJs = (project in file("commonjs")).settings(commonSettings).settings(
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "com.typesafe.play" %%% "play-json" % "2.6.0",
    "org.querki" %%% "jquery-facade" % "1.2",
    "com.github.karasiq" %%% "scalajs-bootstrap" % "2.3.5",
    "com.lihaoyi" %%% "scalatags" % "0.7.0",
  )
)
.enablePlugins(ScalaJSPlugin,ScalaJSWeb)
.dependsOn(sharedJs)
  
  
lazy val mformapp = (project in file("mformapp")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
)
.enablePlugins(ScalaJSPlugin,ScalaJSWeb)
.dependsOn(sharedJs,commonJs)

lazy val homeapp = (project in file("homeapp")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
)
.enablePlugins(ScalaJSPlugin,ScalaJSWeb)
.dependsOn(sharedJs,commonJs)

lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5",
    "org.querki" %%% "jquery-facade" % "1.2",
    "com.typesafe.play" %%% "play-json" % "2.6.0",
    "com.lihaoyi" %%% "scalatags" % "0.7.0",
    "com.github.japgolly.scalajs-react" %%% "core" % "1.4.2",
    "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.2",
  ),
  jsDependencies ++= Seq(
      "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js",
      "org.webjars.npm" % "react" % "16.7.0"
        /        "umd/react.development.js"
        minified "umd/react.production.min.js"
        commonJSName "React",

      "org.webjars.npm" % "react-dom" % "16.7.0"
        /         "umd/react-dom.development.js"
        minified  "umd/react-dom.production.min.js"
        dependsOn "umd/react.development.js"
        commonJSName "ReactDOM",

      "org.webjars.npm" % "react-dom" % "16.7.0"
        /         "umd/react-dom-server.browser.development.js"
        minified  "umd/react-dom-server.browser.production.min.js"
        dependsOn "umd/react-dom.development.js"
        commonJSName "ReactDOMServer"
    ),
  dependencyOverrides += "org.webjars.npm" % "js-tokens" % "3.0.2"
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs,commonJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

lazy val commonSettings = Seq(
  scalaVersion := "2.12.5",
  organization := "com.thatjessicaiknow",
  scalacOptions ++= Seq("-Xfatal-warnings","-feature")
)

// loads the server project at sbt startup
onLoad in Global := (onLoad in Global).value andThen {s: State => "project server" :: s}
