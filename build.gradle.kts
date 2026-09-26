import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    id("java-library")
    id("idea")
    id("maven-publish")
    id("net.neoforged.moddev") version "2.0.146"
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
}

val minecraft_version = providers.gradleProperty("minecraft_version").get()
val minecraft_version_range = providers.gradleProperty("minecraft_version_range").get()
val neo_version = providers.gradleProperty("neo_version").get()
val loader_version_range = providers.gradleProperty("loader_version_range").get()
val mod_id = providers.gradleProperty("mod_id").get()
val mod_name = providers.gradleProperty("mod_name").get()
val mod_license = providers.gradleProperty("mod_license").get()
val mod_version = providers.gradleProperty("mod_version").get()
val mod_group_id = providers.gradleProperty("mod_group_id").get()

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

version = mod_version
group = mod_group_id

sourceSets.named("main") {
    java.srcDir("src/datagen/kotlin")
    resources {
        srcDir("src/generated/resources")
        exclude("**/*.bbmodel")
        exclude("src/generated/**/.cache")
    }
}

repositories {
    mavenCentral()
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
    gradlePluginPortal()
    maven {
        name = "neoforgedReleases"
        url = uri("https://maven.neoforged.net/releases")
    }
}

base {
    archivesName.set(mod_id)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

neoForge {
    version = neo_version
    validateAccessTransformers = true

    runs {
        create("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        create("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        create("clientData") {
            clientData()
            programArguments.addAll(
                "--mod",
                mod_id,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath,
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")
        }
    }

    mods {
        create(mod_id) {
            sourceSet(project.sourceSets.getByName("main"))
        }
    }
}

dependencies {
    implementation("org.jetbrains:annotations:15.0")
    implementation("thedarkcolour:kotlinforforge-neoforge:6.3.0")
}

tasks.withType<ProcessResources>().configureEach {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
    )
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/neoforge.mods.toml")) {
        expand(replaceProperties)
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri(layout.projectDirectory.dir("repo"))
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
