plugins {
  id("com.hiwangzi.luv.kotlin-library-conventions")
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

version = "1.0.0-SNAPSHOT"

val vertxVersion = project.ext.get("vertxVersion")
dependencies {
  implementation("com.hiwangzi.luv:model")
  // service proxies
  implementation("io.vertx:vertx-service-proxy:$vertxVersion")
  compileOnly("io.vertx:vertx-codegen:$vertxVersion")
  kapt("io.vertx:vertx-codegen:$vertxVersion:processor")
  kapt("io.vertx:vertx-service-proxy:$vertxVersion")
  // authentication
  implementation("io.vertx:vertx-auth-jwt:$vertxVersion")
  implementation("io.vertx:vertx-web-validation:$vertxVersion")
  // database
  implementation("io.vertx:vertx-pg-client:$vertxVersion")
  // codec
  implementation("commons-codec:commons-codec:1.15")
}
