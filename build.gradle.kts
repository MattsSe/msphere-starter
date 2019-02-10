import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URI
import java.net.URL
import java.nio.channels.Channels


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

// static endpoint of the mindsphere sdk zip file, may change over time
val sdkEndpoint =
    "https://support.industry.siemens.com/dl/files/603/109757603/att_972175/v1/mindsphere-sdk-java-v2.1.0.zip?download=true"

// destination of the downloaded zip file
val sdkZip = File("./libs/mindsphere-sdk-java.zip")


/**
 * find the folder of the unzipped sdk
 * @throws [FileNotFoundException] if no folder `./libs/midsphere-sdk-java-v{x.y.z}` was found
 * @return the [URI] of the maven repo root
 */
fun localMindSphereRepo(): URI {
    val files = File("./libs").listFiles { file -> file.name.startsWith("mindsphere-sdk-java") && file.isDirectory }
    if (files.size != 1) {
        throw FileNotFoundException("could not find local mindsphere repo `./libs/mindsphere-sdk-java-v{x.y.z}`")
    }
    return uri(files[0].toString())
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

/**
 * download task for the static [sdkEndpoint], if this fails download the sdk file manually
 */
fun downloadSDK() {
    if (sdkZip.exists()) {
        return
    }
    val url = URL(sdkEndpoint)
    val rbc = Channels.newChannel(url.openStream())
    val fos = FileOutputStream(sdkZip)
    fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
}

/**
 * task to unzip a mindsphere sdk file into [sdkZip]
 */
fun unzip() {
    if (sdkZip.exists()) {
        copy {
            from(zipTree(sdkZip))
            into("./libs")
        }
    } else {
        val zips =
            File("./libs").listFiles { file -> file.name.startsWith("mindsphere-sdk-java") && file.endsWith(".zip") }
        if (zips.size == 1) {
            copy {
                from(zipTree(zips[0]))
                into("./libs")
            }
        }
    }
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

    val installSDK by creating(Copy::class) {
        unzip()
    }

    val getSDK by creating(Copy::class) {
        downloadSDK()
        finalizedBy(installSDK)
    }

    val build by tasks.existing {
        unzip()
        dependsOn(copyShadow)
    }

}