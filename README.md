# Full Stack Scala Demo Project
This is a working demo full stack web application created with Scala, Play, Slick and ScalaJS.  The core models that will be shared between the front and back end is in the `shared` project.  The `commonjs` project is shared between the mulitple front ends used by the app.  There's also a generic repository pattern that is used for database access with Slick and using Guice for dependency injection.
## Starting the Application
Ensure that you are using Java version 8 and sbt is installed.  Then run
```shell
sbt update && sbt compile && sbt run
```
