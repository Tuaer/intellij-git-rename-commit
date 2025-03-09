plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "me.jirigebauer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {url = uri("https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository/releases")}
    maven {url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies")}
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("233.11799.241")
    type.set("IU") // Target IDE Platform

    plugins.set(listOf("Git4Idea"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("233.11799")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    buildPlugin {
        dependsOn("patchPluginXml")
    }
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
