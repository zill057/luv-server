// == Define locations for build logic ==
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("../build-logic")
}

rootProject.name = "model"
