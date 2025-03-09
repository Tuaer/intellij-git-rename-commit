import org.jetbrains.kotlin.utils.addToStdlib.enumSetOf

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "me.jirigebauer"
version = "1.0.0"

val intellijVersion = "2023.3"
val javaVersion = "17"

repositories {
    mavenCentral()
    maven {url = uri("https://cache-redirector.jetbrains.com/www.jetbrains.com/intellij-repository/releases")}
    maven {url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies")}
}

intellij {
    version.set(intellijVersion)
    type.set("IU")
    plugins.set(listOf("Git4Idea"))
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
}


tasks {
    withType<JavaCompile> {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        options.encoding = "UTF-8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    patchPluginXml {
        sinceBuild.set("233.11799")
        untilBuild.set("233.*")
        version.set(project.version.toString())
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
        channels.set(listOf("Stable"))
    }

    buildPlugin {
        dependsOn("patchPluginXml")
    }

    runPluginVerifier {
        ideVersions.set(listOf("IU-2023.3"))
        failureLevel.set(
            enumSetOf(
                org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.COMPATIBILITY_PROBLEMS,
                org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.INVALID_PLUGIN
            )
        )
    }

    runIde {
        dependsOn("verifyPluginConfiguration")
    }
}