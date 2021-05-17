import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("kapt")
}

group = "com.hiwangzi.luv"

repositories {
  mavenCentral()
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_11
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()

ext {
  set("vertxVersion", "4.0.3")
  set("junitJupiterVersion", "5.7.0")
}

val vertxVersion = project.ext.get("vertxVersion")
val junitJupiterVersion = project.ext.get("junitJupiterVersion")
dependencies {
  constraints {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.2")
  }
  // vertx base
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
  implementation(kotlin("stdlib-jdk8"))
  // vertx config
  implementation("io.vertx:vertx-config:$vertxVersion")
  // log
  implementation("org.slf4j:slf4j-api:1.7.30")
  runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:2.14.1")
  // test
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

tasks.test {
  useJUnitPlatform()
}
