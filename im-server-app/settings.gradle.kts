// == Define locations for build logic ==
pluginManagement {
  repositories {
    gradlePluginPortal()
  }
  includeBuild("../build-logic")
}

// == Define the inner structure of this component ==
rootProject.name = "im-server-app" // the component name
