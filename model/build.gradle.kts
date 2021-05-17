plugins {
  id("com.hiwangzi.luv.kotlin-library-conventions")
}

version = "1.0.0-SNAPSHOT"

val vertxVersion = project.ext.get("vertxVersion")
dependencies {
  compileOnly("io.vertx:vertx-codegen:$vertxVersion")
  kapt("io.vertx:vertx-codegen:$vertxVersion:processor")
  implementation("com.fasterxml.jackson.core:jackson-databind")
}
