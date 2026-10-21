plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("maven-publish")
    id("dev.kikugie.stonecutter")
}

extra["razeBuild"] = mapOf(
    "loaderConfiguration" to "modImplementation",
    "fabricApiConfiguration" to "modImplementation",
    "compileOnlyConfiguration" to "modCompileOnly",
    "includeMappings" to true
)

apply(from = rootProject.file("gradle/raze.shared.gradle.kts"))

val runClientUsername = (
    findProperty("run_client_username")
        ?: rootProject.findProperty("run_client_username")
        ?: "Player"
).toString()

loom {
    runs {
        named("client") {
            programArg("--username=$runClientUsername")
            vmArg("-Dfabric.development=true")
        }
    }
}
