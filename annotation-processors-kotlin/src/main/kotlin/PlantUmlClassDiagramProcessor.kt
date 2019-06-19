package com.comsysto.livingdoc.kotlin.annotation.processors

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNotes
import com.comsysto.livingdoc.kotlin.annotation.processors.PlantUmlClassDiagramProcessor.KEY_OUT_DIR
import com.comsysto.livingdoc.kotlin.annotation.processors.PlantUmlClassDiagramProcessor.KEY_SETTINGS_DIR
import com.comsysto.livingdoc.kotlin.annotation.processors.model.ClassDiagram
import com.comsysto.livingdoc.kotlin.annotation.processors.model.DiagramId
import com.comsysto.livingdoc.kotlin.annotation.processors.model.TypePart
import com.google.auto.service.AutoService
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass")
@SupportedOptions(KEY_SETTINGS_DIR, KEY_OUT_DIR)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
object PlantUmlClassDiagramProcessor : AbstractProcessor() {
    private val log = LoggerFactory.getLogger(PlantUmlClassDiagramProcessor.javaClass.name);

    const val KEY_SETTINGS_DIR = "pumlgen.settings.dir"
    const val DEF_SETTINGS_DIR = "."
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
        val annotated = roundEnv.getElementsAnnotatedWith(annotation)
                .map { it as TypeElement }
                .toSet()

        when (annotation.qualifiedName.toString()) {
            PlantUmlClass::class.qualifiedName -> processPlantumlClassAnnotation(annotated)

            // Here we could add support for additional top-level annotations

            else -> log.error(
                    "Unexpected annotation type: {}. Most likely there is a mismatch between the value of "
                            + "@SupportedAnnotationTypes and the annotation handling implemented in "
                            + "PlantUmlClassDiagramProcessor.processAnnotation.",
                    annotation.qualifiedName.toString())
        }
    }

    /**
     * Process the [PlantUmlClass] annotation for all types that have this
     * annotation. The processor will generate one diagram file per unique
     * diagram ID found on any annotation.
     *
     * @param annotatedTypes the annotated types.
     */
    private fun processPlantumlClassAnnotation(annotatedTypes: Set<TypeElement>) {
        val generatedFiles = annotatedTypes
                .map { this.createDiagramPart(it) }
                .flatMap { part ->
                    part.diagramIds
                            .map { id -> TypePart(setOf(id), part.annotation, part.typeElement, part.notes) }
                }
                .groupBy { it.diagramIds.first() }

        generatedFiles.entries.forEach { entry -> createDiagram(entry.key, entry.value) }
    }

    private fun createDiagramPart(annotated: TypeElement): TypePart {
        val classAnnotation = annotated.getAnnotation(PlantUmlClass::class.java)
        log.debug("Processing PlantUmlClass annotation on type: {}", annotated)

        return TypePart(
                classAnnotation.diagramIds.map { DiagramId(it) }.toSet(),
                classAnnotation,
                annotated,
                getNoteAnnotations(annotated))
    }

    private fun getNoteAnnotations(annotated: TypeElement): List<PlantUmlNote> {
        return annotated.getAnnotation(PlantUmlNotes::class.java)?.value?.toList()
                ?: listOfNotNull(annotated.getAnnotation(PlantUmlNote::class.java))
    }

    private fun createDiagram(diagramId: DiagramId, parts: List<TypePart>) {
        val outFile = getOutFile(diagramId)
        val settings = loadSettings(diagramId)

        log.debug("Create PlantUML diagram: {}", outFile)

        try {
            BufferedWriter(FileWriter(outFile)).use { out ->
                ClassDiagram(
                        settings.getProperty("title", null),
                        settings.getProperty("include.files", "")
                                .split(",")
                                .dropLastWhile { it.isEmpty() },
                        parts
                ).render()
            }
        } catch (e: IOException) {
            log.error("Failed to generate diagram: $outFile", e)
            throw RuntimeException(e)
        }
    }

    private fun getOutFile(diagramId: DiagramId): File {
        val diagramFile = File(outDir(), diagramId.value + "_class.puml")

        diagramFile.parentFile.mkdirs()
        return diagramFile
    }

    private fun loadSettings(id: DiagramId): Properties {
        val settingsFile = File(settingsDir(), id.value + "_class.properties")
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