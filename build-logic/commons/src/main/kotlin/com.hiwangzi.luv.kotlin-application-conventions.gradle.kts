plugins {
  id("com.hiwangzi.luv.kotlin-common-conventions")
  application
}

tasks.register("apidoc") {
  project.exec {
    setWorkingDir("${project.projectDir}/docs")
    executable("apidoc")
    args("-i", "${project.projectDir}/src/main")
    args("-o", "${project.buildDir}/apidoc")
    args("-c", "${project.projectDir}/docs")
    args("-t", "${project.projectDir}/docs/template")
  }
}
