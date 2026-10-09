plugins {
    id("fabric-loom") version "1.18-SNAPSHOT"
    `maven-publish`
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

repositories {
    // Loom adds the essential maven repositories to download Minecraft and
    // libraries from automatically. No extra repositories needed.
}

dependencies {
    // Same versions as MTN-template (Fabric 1.21.1).
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

// Publish to Maven Local only — so the artifact can be consumed during
// development (./gradlew publishToMavenLocal). JitPack handles the remote
// side; no remote repository is configured here.
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.processResources {
    val version = project.version
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and
    // to the "build" task if it is present.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
    val projectName = project.name
    // Normal Fabric jar: no shadow, no relocation, no fat jar.
    from("README.md") {
        rename { "${it}_${projectName}" }
    }
}
