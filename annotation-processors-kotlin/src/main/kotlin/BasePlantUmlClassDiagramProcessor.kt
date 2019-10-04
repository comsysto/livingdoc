package com.comsysto.livingdoc.kotlin.annotation.processors

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNotes
import com.comsysto.livingdoc.kotlin.annotation.processors.model.ClassDiagram
import com.comsysto.livingdoc.kotlin.annotation.processors.model.DiagramId
import com.comsysto.livingdoc.kotlin.annotation.processors.model.TypePart
import com.comsysto.livingdoc.kotlin.annotation.processors.render.plantuml.PlantUmlDiagramRenderer.renderDiagram
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

open class BasePlantUmlClassDiagramProcessor : AbstractProcessor() {
    private val log = LoggerFactory.getLogger(BasePlantUmlClassDiagramProcessor::class.java.name);

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
            PlantUmlClass::class.qualifiedName -> renderDiagrams(annotated.map { createDiagramPart(it) }, roundEnv)

            else -> log.error(
                    "Unexpected annotation type: {}. Most likely there is a mismatch between the value of "
                            + "@SupportedAnnotationTypes and the annotation handling implemented in "
                            + "PlantUmlDiagramProcessor.processAnnotation.",
                    annotation.qualifiedName.toString())
        }
    }
    
    private fun renderDiagrams(parts: List<TypePart>, roundEnv: RoundEnvironment) {
        val generatedFiles = parts.map { it.typeElement }

        generatedFiles.entries.forEach { entry -> createDiagram(entry.key, entry.value) }
    }

    private fun createDiagramPart(annotated: TypeElement): TypePart {
        val classAnnotation = annotated.getAnnotation(PlantUmlClass::class.java)
        log.debug("Processing PlantUmlClass annotation on type: {}", annotated)

        return TypePart(classAnnotation, annotated, getNoteAnnotations(annotated))
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
                val classDiagram = ClassDiagram(
                        settings.getProperty("title", null),
                        settings.getProperty("include.files", "")
                                .split(",")
                                .dropLastWhile { it.isEmpty() },
                        parts
                )
                out.write(renderDiagram(classDiagram))
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
