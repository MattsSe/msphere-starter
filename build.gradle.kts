import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    java
    application
    kotlin("jvm") version "1.3.20"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

group = "io.github.mattsse"
version = "0.1.0"

application {
    applicationName = "create-msphere"
    mainClassName = "io.github.mattsse.msphere.gen.ProjectGeneratorKt"
}

tasks.withType<ShadowJar> {
    baseName = "create-msphere"
    classifier = ""
    version = project.version.toString()

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
repositories {
    mavenCentral()
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.ajalt:clikt:1.6.0")
    testCompile("junit", "junit", "4.12")
}

tasks {

    val copyShadow by registering(ShadowJar::class) {
        doLast {
            copy {
                from("build/libs/create-msphere-${project.version}.jar")
                into("create-msphere")
            }
        }
    }

    val build by tasks.existing {
        dependsOn(copyShadow)
    }

}