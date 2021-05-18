plugins {
  id("com.hiwangzi.luv.kotlin-library-conventions")
}

version = "1.0.0-SNAPSHOT"

val vertxVersion = project.ext.get("vertxVersion")
dependencies {
  // module
  implementation("com.hiwangzi.luv:model")
  implementation("com.hiwangzi.luv:database")
  // authentication
  implementation("io.vertx:vertx-auth-jwt:$vertxVersion")
  // web
  implementation("io.vertx:vertx-web:$vertxVersion")
  // bcrypt
  implementation("at.favre.lib:bcrypt:0.9.0")
}
