package com.comsysto.livingdoc.kotlin.annotation.processors

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNotes
import com.comsysto.livingdoc.kotlin.annotation.processors.model.DiagramId
import com.comsysto.livingdoc.kotlin.annotation.processors.model.SequenceDiagram
import com.comsysto.livingdoc.kotlin.annotation.processors.render.plantuml.PlantUmlDiagramRenderer
import com.google.auto.service.AutoService
import model.ExecutablePart
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement


@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable.StartOfSequence")
@SupportedOptions(KEY_SETTINGS_DIR, KEY_OUT_DIR)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
object PlantUmlSequenceDiagramProcessor : AbstractProcessor() {
    private val log = LoggerFactory.getLogger(PlantUmlSequenceDiagramProcessor::class.java.name);

    const val KEY_SETTINGS_DIR = "pumlgen.settings.dir"
    const val DEF_SETTINGS_DIR = ".."
    const val KEY_OUT_DIR = "pumlgen.out.dir"
    const val DEF_OUT_DIR = "./out"

    private fun settingsDir() = processingEnv.options.getOrDefault(KEY_SETTINGS_DIR, DEF_SETTINGS_DIR)!!
    private fun outDir() = processingEnv.options.getOrDefault(KEY_OUT_DIR, DEF_OUT_DIR)!!

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        log.debug("Environment: {}", processingEnv.options.toString())

        if (!roundEnv.errorRaised() && !roundEnv.processingOver()) {
            annotations.forEach { annotation -> processAnnotation(annotation, roundEnv) }
            return true
        }
        return false
    }

    /**
     * Process a specific annotation.
     *
     * @param annotation the annotation
     * @param roundEnv   the round environment
     */
    private fun processAnnotation(annotation: TypeElement, roundEnv: RoundEnvironment) {
        when (annotation.qualifiedName.toString()) {
            PlantUmlExecutable.StartOfSequence::class.qualifiedName -> processStartOfSequence(roundEnv.getElementsAnnotatedWith(annotation)
                    .map { it as ExecutableElement }
                    .toSet())

            else -> log.error(
                    "Unexpected annotation type: {}. Most likely there is a mismatch between the value of "
                            + "@SupportedAnnotationTypes and the annotation handling implemented in "
                            + "PlantUmlDiagramProcessor.processAnnotation.",
                    annotation.qualifiedName.toString())
        }
    }

    private fun processStartOfSequence(annotated: Set<ExecutableElement>) {
        val generatedFiles = annotated
                .map { this.createDiagramPart(it) }
                .flatMap { part ->
                    part.diagramIds
                            .map { id ->
                                ExecutablePart(
                                        processingEnv,
                                        setOf(id),
                                        part.annotation,
                                        part.annotated)
                            }
                }
                .groupBy { it.diagramIds.take(1)[0] }

        generatedFiles.keys.forEach { id ->
            generatedFiles[id]?.let { files -> createSequenceDiagram(id, files) }
        }
    }

    private fun createSequenceDiagram(diagramId: DiagramId, parts: List<ExecutablePart>) {
        val outFile = getOutFile(diagramId)
        val settings = loadSettings(diagramId)

        log.debug("Create PlantUML diagram: {}", outFile)

        BufferedWriter(FileWriter(outFile)).use { out ->
            BufferedWriter(FileWriter(outFile)).use { out ->
                val sequenceDiagram = SequenceDiagram(
                        settings.getProperty("title", null),
                        settings.getProperty("include.files", "")
                                .split(",")
                                .dropLastWhile { it.isEmpty() },
                        parts)
                out.write(PlantUmlDiagramRenderer.renderDiagram(sequenceDiagram))
            }
        }
    }

    private fun createDiagramPart(annotated: ExecutableElement): ExecutablePart {
        val annotation = annotated.getAnnotation(PlantUmlExecutable::class.java)
        log.debug("Processing PlantUmlExecutable annotation on executable: {}", annotated)

        return ExecutablePart(
                processingEnv,
                annotation.diagramIds
                        .map { DiagramId(it) }
                        .toSet(),
                annotation,
                annotated)
    }

    private fun getNoteAnnotations(annotated: TypeElement): List<PlantUmlNote> {
        return annotated.getAnnotation(PlantUmlNotes::class.java)?.value?.toList()
                ?: listOfNotNull(annotated.getAnnotation(PlantUmlNote::class.java))
    }

    private fun getOutFile(diagramId: DiagramId): File {
        val diagramFile = File(outDir(), diagramId.value + "_sequence.puml")

        diagramFile.parentFile.mkdirs()
        return diagramFile
    }

    private fun loadSettings(id: DiagramId): Properties {
        val settingsFile = File(settingsDir(), id.value + "_sequence.properties")
        val settings = Properties()

        try {
            FileReader(settingsFile).use { `in` ->
                log.debug("Settings file: {}", settingsFile.absoluteFile)
                settings.load(`in`)
                log.debug("Settings: \n{}", settings.toString())
            }
        } catch (e: IOException) {
            log.debug("No settings file found: {}", settingsFile.absoluteFile)
        }
        return settings
    }
}
