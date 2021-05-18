import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  id("com.hiwangzi.luv.kotlin-application-conventions")
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

version = "1.0.0-SNAPSHOT"

val mainVerticleName = "com.hiwangzi.luv.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "cd ${projectDir}/.. && ./gradlew :${rootProject.name}:classes"

application {
  project.setProperty("mainClassName", launcherClassName)
}

val vertxVersion = project.ext.get("vertxVersion")
dependencies {
  // module
  implementation("com.hiwangzi.luv:model")
  implementation("com.hiwangzi.luv:database") // only for starting verticle
  implementation("com.hiwangzi.luv:auth-feature")
  implementation("com.hiwangzi.luv:user-feature")
  implementation("com.hiwangzi.luv:im-feature")
  // json
  implementation("com.fasterxml.jackson.core:jackson-databind:2.12.2")
  // web
  implementation("io.vertx:vertx-web:$vertxVersion")
  // test
  testImplementation("io.vertx:vertx-web-client:$vertxVersion'")
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<JavaExec> {
  args = listOf(
    "run",
    mainVerticleName,
    "--redeploy=$watchForChange",
    "--launcher-class=$launcherClassName",
    "--on-redeploy=$doOnChange",
    // used for attaching remote debugger
    "--java-opts", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
  )
}
