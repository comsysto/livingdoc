package com.comsysto.livingdoc.s0t.render

import com.comsysto.livingdoc.s0t.model.S0tModel
import com.comsysto.livingdoc.s0t.*
import com.comsysto.livingdoc.s0t.S0tProcessor.Companion.environment
import freemarker.cache.ClassTemplateLoader
import freemarker.cache.FileTemplateLoader
import freemarker.cache.MultiTemplateLoader
import freemarker.template.Configuration
import freemarker.template.Configuration.VERSION_2_3_23
import freemarker.template.Template
import org.apache.commons.configuration2.MapConfiguration
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

object FreemarkerRenderer : OutputRenderer {
    private val log = LoggerFactory.getLogger(FreemarkerRenderer::class.java.name)

    private const val CUSTOM_ATTRIBUTE_PREFIX = "s0t:"
    private const val KEY_IS_TEMPLATE = "is-template"
    private const val KEY_OUTPUT_FILENAME = "output.filename"
    private const val KEY_OUTPUT_DEFAULT_EXTENSION = "output.default-extension"
    private const val DEF_OUTPUT_DEFAULT_EXTENSION = "puml"
    private const val KEY_ACCEPTED_OUTPUT_EXTENSIONS = "output.accepted-extensions"
    private const val DEF_ACCEPTED_OUTPUT_EXTENSIONS = "puml,iuml,md,adoc"
    private const val KEY_DIAGRAM_ID = "diagram-id"
    private const val DEF_DIAGRAM_ID = "default"
    private const val STANDARD_TEMPLATES_DIRECTORY = "com/comsysto/livingdoc/s0t"


    override fun render(model: S0tModel) {
        val env = environment().processingEnvironment
        val templateDirectory = environment().resolveConfiguredPath(KEY_TPL_DIR)?.toFile()

        if (templateDirectory != null) {
            val outputDirectory = environment().outputPath()!!.toFile()
            val templateExtension = env.options.getOrDefault(KEY_TPL_EXT, DEF_TPL_EXT)
            val freemarker = Configuration(VERSION_2_3_23)

            freemarker.templateLoader = MultiTemplateLoader(arrayOf(
                FileTemplateLoader(templateDirectory),
                ClassTemplateLoader(FreemarkerRenderer::class.java.classLoader, STANDARD_TEMPLATES_DIRECTORY)))
            freemarker.addAutoImport("S0tTypes", "s0t-types.ftl")
            freemarker.addAutoImport("S0tMembers", "s0t-members.ftl")
            freemarker.addAutoImport("S0tRelations", "s0t-relations.ftl")
            freemarker.addAutoImport("S0tSequences", "s0t-sequences.ftl")

            templateDirectory.walkTopDown()
                .filter { it.isFile && it.name.endsWith(templateExtension) }
                .forEach { loadAndRenderTemplate(it, freemarker, templateDirectory, outputDirectory, templateExtension, model) }
        }
        else {
            log.debug("Skipping s0t freemarker generation: no template directory defined through {}", KEY_TPL_DIR)
        }
    }

    private fun loadAndRenderTemplate(
            templateFile: File,
            freemarker: Configuration,
            templateDirectory: File,
            outputDirectory: File,
            templateExtension: String,
            model: S0tModel
    )
    {
        val template = freemarker.getTemplate(templateFile.toRelativeString(templateDirectory))
        val cfg = s0tConfiguration(template)

        if (cfg.getBoolean(KEY_IS_TEMPLATE, false)) {
            val outputFile = File(outputDirectory, outputFileName(cfg, templateFile, templateExtension, templateDirectory))
                    .apply { parentFile.mkdirs() }
            val diagramId = cfg.getString(KEY_DIAGRAM_ID, DEF_DIAGRAM_ID)

            log.info("Rendering template {} to {}", template.name, outputFile)

            template.process(
                PlantUmlModel(
                    diagramId,
                    templateDirectory.absolutePath,
                    outputDirectory.absolutePath,
                    model.filter(diagramId)),
                outputFile.bufferedWriter(UTF_8))
        }
    }

    /**
     * Get the s0t-specific template configuration from the template's custom
     * properties.
     */
    private fun s0tConfiguration(template: Template): MapConfiguration = MapConfiguration(template.customAttributeNames
            .filter { it.startsWith(CUSTOM_ATTRIBUTE_PREFIX) }
            .groupBy({ it.substringAfter(CUSTOM_ATTRIBUTE_PREFIX) }, { template.getCustomAttribute(it) }))

    private fun outputFileName(cfg: MapConfiguration, it: File, templateExtension: String, outputDirectory: File): String {
        return cfg.getString(KEY_OUTPUT_FILENAME, defaultOutputFile(cfg, it, templateExtension, outputDirectory).path)
    }

    private fun defaultOutputFile(cfg: MapConfiguration, it: File, templateExtension: String, templateDirectory: File): File {
        val filename = it.name.substringBeforeLast(templateExtension)

        return File(it.parentFile, if (hasAcceptedExtension(filename, cfg)) filename else filename + defaultExtension(cfg))
                .relativeTo(templateDirectory)
    }

    private fun hasAcceptedExtension(filename: String, cfg: MapConfiguration) = acceptedExtensions(cfg).any { filename.endsWith(".${it}") }
    private fun acceptedExtensions(cfg: MapConfiguration) = cfg.getString(KEY_ACCEPTED_OUTPUT_EXTENSIONS, DEF_ACCEPTED_OUTPUT_EXTENSIONS).split(",")
    private fun defaultExtension(cfg: MapConfiguration) = cfg.getString(KEY_OUTPUT_DEFAULT_EXTENSION, DEF_OUTPUT_DEFAULT_EXTENSION)
}
