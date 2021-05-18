plugins {
  id("com.hiwangzi.luv.kotlin-library-conventions")
}

version = "1.0.0-SNAPSHOT"

val vertxVersion = project.ext.get("vertxVersion")
dependencies {
  // module
  implementation("com.hiwangzi.luv:model")
  implementation("com.hiwangzi.luv:database")
}
