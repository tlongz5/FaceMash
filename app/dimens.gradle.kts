import java.io.File
import java.io.PrintWriter
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal

abstract class SDPFactory : DefaultTask() {
    @get:Internal
    abstract val resFolder: DirectoryProperty

    @get:Input
    var unit: String = "dp"

    @get:Input
    var positiveMax: Double = 600.0

    @get:Input
    var negativeMax: Double = 60.0

    @TaskAction
    fun create() {
        val resFolderPath = resFolder.get().asFile.absolutePath + File.separator

        for (dimen in 300..1080 step 30) {
            val folder = "${resFolderPath}values-sw${dimen}dp"
            val fileName = "$folder/positive_sdps.xml"
            File(folder).mkdirs()
            File(fileName).createNewFile()
            PrintWriter(fileName).use { writer ->
                writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                writer.println("<resources>")
                for (i in 1..positiveMax.toInt()) {
                    val ratio = i / 330.0
                    val sdp = ratio * dimen
                    writer.printf("\t<dimen name=\"_%dsdp\">%.2f%s</dimen>\r\n", i, sdp, unit)
                }
                writer.println("</resources>")
            }
        }

        for (dimen in 300..1080 step 30) {
            val folder = "${resFolderPath}values-sw${dimen}dp"
            val fileName = "$folder/negative_sdps.xml"
            File(folder).mkdirs()
            File(fileName).createNewFile()
            PrintWriter(fileName).use { writer ->
                writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                writer.println("<resources>")
                for (i in 1..negativeMax.toInt()) {
                    val ratio = i / 330.0
                    val sdp = ratio * dimen
                    writer.printf("\t<dimen name=\"_minus%dsdp\">%.2f%s</dimen>\r\n", i, -sdp, unit)
                }
                writer.println("</resources>")
            }
        }
    }
}

tasks.register<SDPFactory>("createSDP") {
    resFolder.set(project.layout.projectDirectory.dir("src/main/res"))
    unit = "dp"
    positiveMax = 600.0 // change to 600 or any other value if needed
    negativeMax = 60.0 // change to 600 or any other value if needed
}

abstract class SSPFactory : DefaultTask() {
    @get:Internal
    abstract val resFolder: DirectoryProperty

    @get:Input
    var unit: String = "sp"

    @get:Input
    var positiveMax: Double = 100.0

    @TaskAction
    fun create() {
        val resFolderPath = resFolder.get().asFile.absolutePath + File.separator
        for (dimen in 300..1080 step 30) {
            createPositive(resFolderPath, dimen.toDouble())
        }
    }

    private fun createPositive(resFolder: String, dimen: Double) {
        val folder = "${resFolder}values-sw${dimen.toInt()}dp"
        val fileName = "$folder/positive_ssps.xml"
        File(folder).mkdirs()
        File(fileName).createNewFile()
        PrintWriter(fileName).use { writer ->
            writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
            writer.println("<resources>")
            for (i in 1..positiveMax.toInt()) {
                val ratio = i / 300.0
                val ssp = ratio * dimen
                writer.printf("\t<dimen name=\"_%dssp\">%.2f%s</dimen>\r\n", i, ssp, unit)
            }
            writer.println("</resources>")
        }
    }
}

tasks.register<SSPFactory>("createSSP") {
    resFolder.set(project.layout.projectDirectory.dir("src/main/res"))
    unit = "sp"
    positiveMax = 100.0
}