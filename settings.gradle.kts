rootProject.name = "luv-server"

includeBuild("build-logic")

includeBuild("model")
includeBuild("database")

includeBuild("auth-feature")
includeBuild("user-feature")
includeBuild("im-feature")

includeBuild("api-server-app")
