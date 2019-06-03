package io.github.mattsse.msphere.gen

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import io.github.mattsse.msphere.gen.Consts.SDK_ENDPOINT
import io.github.mattsse.msphere.gen.Consts.VERSION
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * Some Helper constants
 */
object Consts {
    const val VERSION = "0.1.0"
    const val SDK_ENDPOINT =
        "https://support.industry.siemens.com/dl/files/603/109757603/att_972175/v1/mindsphere-sdk-java-v2.1.0.zip?download=true"
}

/**
 * Asks the user to put in a value via [readLine]
 * @param default, the value to return if the [readLine] returned an emtpy string
 * @return the user input or [default] if [readLine] was empty
 */
fun String.ask(default: String = ""): String {
    print(this)
    val line = readLine()!!.trim()
    return if (line.isEmpty()) default else line
}

fun fail(message: String): Nothing = throw BadParameterValue(message)

/**
 * Commandline Options for the [ProjectGenerator]
 */
class Gen : CliktCommand() {
    val dest by option("--dest", "-d", help = "The path for new project").file()
    val name by option("--name", "-n", help = "The name of the new project")
    val groupId by option("--group", "-p", help = "The group name")
    val artifactId by option("--artifact", "-a", help = "The artifact name of the project")
    val headless by option(help = "The name of the new project").flag("--headless", "-l", default = false)
    val version by option(help = "Output the version number").flag("--version", "-v", default = false)
    val interactive by option(help = "Create a new project interactively").flag("--interactive", "-i", default = false)
    val kotlin by option(help = "Whether to dest kotlin").flag("--kotlin", "-k", default = true)
    val gradle by option(help = "Whether to install gradle").flag("--gradle", "-g", default = true)
    val gradleVersion by option("--wrapper", "-w", help = "The targeted gradle wrapper version").default("4.10")
    val sdkEndpoint by option("--endpoint", "-e", help = "The url endpoint of the sdk").default(SDK_ENDPOINT)

    /**
     * throws exceptions if the destination is not readable, writable or an existing file
     * @return the destination of the to be created project
     */
    fun askDest(): File {
        val path = "Enter the path for the new project:".ask()
        val file = File(path)
        if (!file.exists()) return file
        if (file.isFile) fail("${file.name} is a file.")
        if (!file.canWrite()) fail("${file.name} is not writable.")
        if (!file.canRead()) fail("${file.name} is not readable.")
        return file
    }

    /**
     * @return a new [Config] by asking for values via [readLine]
     */
    fun interactive(): Config {
        val file = askDest()

        val projectName =
            "Enter the project name [${file.nameWithoutExtension}]:".ask(default = file.nameWithoutExtension)

        val usekotlin = "Use kotlin for the project [Y/n]:".ask(default = "y").toLowerCase() == "y"
        val useGradle = "Use gradle for the project [Y/n]:".ask(default = "y").toLowerCase() == "y"

        val groupId = "Enter the project's group id [$projectName]".ask(default = projectName)
        val artifactId = "Enter the project's group id [$projectName]".ask(default = projectName)

        return Config(
            dest = file,
            name = projectName,
            kotlin = usekotlin,
            useGradle = useGradle,
            gradleVersion = gradleVersion,
            headless = headless,
            group = groupId,
            artifact = artifactId,
            sdkEndpoint = sdkEndpoint
        )


    }

    override fun run() {
        if (version) {
            println("""create-msphere version "$VERSION""")
            System.exit(1)
        }

        val config = if (interactive) {
            interactive()
        } else {
            val file = dest ?: askDest()
            val projectName = name ?: file.nameWithoutExtension
            val groupId = groupId ?: file.nameWithoutExtension
            Config(
                dest = file,
                name = projectName,
                kotlin = kotlin,
                useGradle = gradle,
                gradleVersion = gradleVersion,
                headless = headless,
                group = groupId,
                artifact = artifactId,
                sdkEndpoint = sdkEndpoint
            )
        }

        ProjectGenerator(config).generate()
    }
}

/**
 * Helper class that stores all config options
 */
data class Config(
    val dest: File,
    val name: String,
    val kotlin: Boolean = true,
    val gradleVersion: String = "4.10",
    val useGradle: Boolean = true,
    val headless: Boolean = false,
    val group: String,
    val artifact: String?,
    val sdkEndpoint: String
)

/**
 * appends the [child] to itself and creates a new [File] object
 */
fun File.child(child: String): File = File(this, child)

fun File.mkdirs(children: Array<String>) {
    for (f in children)
        this.child(f).mkdirs()
}

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()
        .waitFor(10, TimeUnit.MINUTES)
}

/**
 *
 */
class ProjectGenerator(val config: Config) {


    init {
        // targeted directory already exists, ask whether to continue anyway
        if (config.dest.exists()) {
            if (config.headless) {
                System.exit(-1)
            }

            val answer =
                "Targeted directory `${config.dest}` already exists. Want to continue anyway? [Y/n]".ask()
                    .toLowerCase()
            if (!arrayOf("y", "yes", "").contains(answer)) {
                System.exit(-1)
            }
        }
    }

    /**
     * initializes git environment
     */
    fun initGit() {
        Template.Ignore.generate(config.dest)
        Template.IgnoreLibs.generate(config.dest.child("libs"))
        "git init".runCommand(config.dest)
    }

    /**
     * generates the gradle wrapper scripts
     */
    fun genGradlew() {
        Template.GradlewSh.generate(config.dest)
        Template.GradlewCmd.generate(config.dest)
        Template.GradlewJar.generate(config.dest)
        Template.GradlewProperties.generate(config.dest)
    }


    /**
     * @return the executable for gradle wrapper
     */
    fun gradlewExe() =
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) ".\\gradlew.bat" else "./gradlew"

    /**
     * initializes the gradle wrapper
     */
    fun initWrapper() =
        "${gradlewExe()} wrapper --gradle-version ${config.gradleVersion}".runCommand(config.dest)

    /**
     * generates the gradle environment
     */
    fun genGradle() {
        Template.Properties.generate(config.dest)
        config.dest.child("gradle/wrapper").mkdirs()
        Template.Settings(config.name).generate(config.dest)
        Template.BuildKts(endpoint = config.sdkEndpoint, group = config.group).generate(config.dest)
        genGradlew()
        println("installing gradle wrapper...")
        initWrapper()
        println("downloading SDK...")
        "${gradlewExe()} getSDK".runCommand(config.dest)
    }

    /**
     * generates the src dirs
     */
    fun genSrc() {
        val src = config.dest.child("src")
        if (config.kotlin) {
            src.mkdirs(arrayOf("main/kotlin", "test/kotlin"))
        }
        config.dest.child("libs").mkdir()

        src.mkdirs(arrayOf("main/java", "main/resources", "test/java", "test/resources"))
    }

    /**
     * Executes the gen process for a new msphere project
     */
    fun generate() {
        genSrc()
        Template.Readme(config.name).generate(config.dest)

        initGit()
        if (config.useGradle) {
            genGradle()
        }
    }
}


fun main(args: Array<String>) {
    Gen().main(args)
}

