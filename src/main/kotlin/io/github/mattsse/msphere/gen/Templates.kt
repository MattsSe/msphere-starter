package io.github.mattsse.msphere.gen

import java.io.File

/**
 *
 */
sealed class Template {

    object Ignore : Template() {

        override fun content() = """.idea/
.gradle
build/
dest/
out/
bin/

gradle-app.setting

!gradle-wrapper.jar

.gradletasknamecache

# # Work around https://youtrack.jetbrains.com/issue/IDEA-116898
# gradle/wrapper/gradle-wrapper.properties"""

        override fun path() = File(".gitignore")
    }

    object IgnoreLibs : Template() {

        override fun content() = """*
!.gitignore
"""

        override fun path() = File(".gitignore")
    }

    object Properties : Template() {

        override fun content() = "kotlin.code.style=official"

        override fun path() = File("gradle.properties")
    }

    object Travis : Template() {

        override fun content() = """language: java
install: true

jdk:
  - oraclejdk8

script:
  - ./gradlew check

before_cache:
  - rm -f ${"$"}HOME/.gradle/caches/modules-2/modules-2.lock
  - rm -fr ${"$"}HOME/.gradle/caches/*/plugin-resolution/

cache:
  directories:
    - ${"$"}HOME/.gradle/caches/
    - ${"$"}HOME/.gradle/wrapper/
"""

        override fun path() = File(".travis.yml")
    }

    data class Settings(val name: String) : Template() {
        override fun content() = """rootProject.name = "$name""""

        override fun path() = File("build.settings.kts")
    }

    object GradlewSh : Template() {

        override fun content() = GradlewSh::class.java.getResource("/gradlew").readText()

        override fun path() = File("gradlew")

        override fun generate(target: File): File {
            val file = super.generate(target)
            file.setExecutable(true)
            return file
        }
    }

    object GradlewCmd : Template() {

        override fun content() = GradlewCmd::class.java.getResource("/gradlew.bat").readText()

        override fun path() = File("gradlew.bat")

        override fun generate(target: File): File {
            val file = super.generate(target)
            file.setExecutable(true)
            return file
        }
    }

    object GradlewJar : Template() {

        override fun content() = ""

        override fun path() = File("gradle/wrapper/gradle-wrapper.jar")

        override fun generate(target: File): File {
            val file = File(target, path().toString())
            if (!file.exists()) {
                file.writeBytes(GradlewJar::class.java.getResource("/gradle-wrapper.jarx").readBytes())
            }
            return file
        }
    }

    object GradlewProperties : Template() {

        override fun content() = GradlewProperties::class.java.getResource("/gradle-wrapper.properties").readText()

        override fun path() = File("gradle/wrapper/gradle-wrapper.properties")

    }

    data class BuildKts(val endpoint: String, val group: String) : Template() {

        override fun content() = BuildKts::class.java.getResource("/build.gradle.txt").readText().replace(
            "{endpoint}",
            endpoint
        ).replace("{group}", group)

        override fun path() = File("build.gradle.kts")
    }

    data class Readme(val projectname: String) : Template() {

        override fun content() = Readme::class.java.getResource("/README.md").readText().replace(
            "{projectname}",
            projectname
        )

        override fun path() = File("README.md")
    }

    abstract fun content(): String

    abstract fun path(): File

    /**
     * @param target the dest directory
     */
    open fun generate(target: File): File {
        val file = File(target, path().toString())
        if (!file.exists()) {
            file.writeText(content())
        }
        return file
    }
}