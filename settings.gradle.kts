pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "neoforgedReleases"
            url = uri("https://maven.neoforged.net/releases")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 插件注册的仓库会排在 build.gradle.kts 声明之前，先把 Maven Central 注册进去，
// 保证 NeoForged 镜像（CDN77）不可达时 Kotlin/Java 依赖仍能解析。
gradle.beforeProject {
    repositories.mavenCentral()
}
