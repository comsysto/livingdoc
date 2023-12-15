package com.comsysto.livingdoc.s0t

import com.comsysto.livingdoc.s0t.render.OutputRenderer
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.ConfigurationUtils
import org.apache.commons.configuration2.YAMLConfiguration
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.tools.JavaFileManager
import javax.tools.StandardLocation
import kotlin.reflect.full.createInstance

/**
 * The S0T environment.
 */
data class Environment(

    /**
     * The global annotation processing environment.
     */
    val processingEnvironment: ProcessingEnvironment,

    /**
     * The annotation processing environment for the currently running round.
     */
    val roundEnvironment: RoundEnvironment
) {
    /**
     * The root path containing the YAML configuration file. Unless explicitly
     * set through the annotation processor option 's0t.src.dir', this will be
     * the source root.
     */
    val root: Path

    /**
     * An Apache Commons Configuration read from the file 's0t.yaml' in the root
     * directory.
     */
    val configuration: Configuration

    val outputRenderers: List<OutputRenderer>

    init {
        val configurationPath: Path = configFileInUserSpecifiedPath()
            ?: (configFileInSourcePath()
                ?: throw IllegalStateException("Failed to determine root path."))

        configuration = YAMLConfiguration()

        if (configurationPath.toFile().exists()) {
            configuration.read(Files.newBufferedReader(configurationPath))
        }
        addOptionsToConfiguration()

        root = configurationPath.parent.toAbsolutePath()
        outputRenderers = configuration.getString(KEY_RENDERERS).split(",")
            .map { rendererClassName -> Class.forName(rendererClassName.trim()) }
            .map { c -> c.kotlin.createInstance() as OutputRenderer }
    }

    private fun configFileInSourcePath() = readonlyFilerPath(StandardLocation.SOURCE_PATH, "", CONFIG_FILE_NAME)

    private fun configFileInUserSpecifiedPath() = processingEnvironment.options[KEY_SRC_DIR]?.let { Paths.get(it).resolve(CONFIG_FILE_NAME) }

    private fun addOptionsToConfiguration() {
        processingEnvironment.options.entries.forEach { configuration.setProperty(it.key, it.value) }
    }

    /**
     * Return the path of the directory containing the specified type.
     *
     * @param packageName the package name.
     * @param simpleTypeName the simple name of the type.
     *
     * @return the path of the type's source file as resolved from the root
     * path.
     */
    fun sourcePath(packageName: String, simpleTypeName: String): Path? =
        listOf(StandardLocation.SOURCE_PATH)
            .map { readonlyFilerPath(it, packageName, "${simpleTypeName}.java") }
            .find { it != null }

            ?: root.resolve(Paths.get("${packageName.replace(".", "/")}/${simpleTypeName}.java"))

    /**
     * Return the output directory.
     */
    fun outputPath() = resolveConfiguredPath(KEY_OUT_DIR, DEF_OUT_DIR)

    private fun readonlyFilerPath(location: JavaFileManager.Location, packageName: String, fileName: String): Path? {
        return try {
            processingEnvironment.filer.getResource(location, packageName, fileName)?.let { Paths.get(it.toUri()) }
        } catch (e: Exception) {
            log.debug("Could not find {}.{} in standard location {}: {}", packageName, fileName, location.name, e.message)
            null
        }
    }

    fun resolveConfiguredPath(configurationKey: String, defaultPath: String? = null): Path? =
        configuration.getString(configurationKey, defaultPath)?.let { root.resolve(Paths.get(it)) }

    override fun toString(): String {
        return "Environment(\n" +
                "  root=$root,\n" +
                "  configuration=${ConfigurationUtils.toString(configuration)},\n" +
                "  processingEnvironment=$processingEnvironment,\n" +
                "  roundEnvironment=$roundEnvironment" +
                ")"
    }


    companion object {
        private val log = LoggerFactory.getLogger(Environment::class.java.name)
    }
}