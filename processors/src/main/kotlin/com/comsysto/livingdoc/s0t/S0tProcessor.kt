package com.comsysto.livingdoc.s0t

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable
import com.comsysto.livingdoc.s0t.model.ExecutableModel
import com.comsysto.livingdoc.s0t.model.S0tModel
import com.comsysto.livingdoc.s0t.model.TypeModel
import com.comsysto.livingdoc.s0t.render.DebugRenderer
import com.comsysto.livingdoc.s0t.render.FreemarkerRenderer
import com.comsysto.livingdoc.s0t.render.OutputRenderer
import com.google.auto.service.AutoService
import org.slf4j.LoggerFactory
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * The S0T annotation processor.
 */
@SupportedAnnotationTypes(
    "com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass",
    "com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable.StartOfSequence"
)
@SupportedOptions(KEY_OUT_DIR)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
class S0tProcessor : AbstractProcessor() {

    /**
     * The (mutable) model built from the sources.
     */
    internal var model: S0tModel = S0tModel() 

    /**
     * The list of output renderers registered with the processor.
     */
    private val outputRenderers: List<OutputRenderer> = listOf(DebugRenderer, FreemarkerRenderer)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        log.info("S0T called with options: {}", processingEnv.options.toString())
        if (!roundEnv.processingOver() && !roundEnv.errorRaised()) {
            createEnvironment(roundEnv)?.let {
                log.info("Starting S0T processing with environment: {}", it)
                threadLocalEnv.set(it)
                buildModel(annotations)
                renderOutput()
                return true
            }
        }
        log.info("Processing finished {}.", if (roundEnv.errorRaised()) "with errors" else "successfully")
        return true
    }

    private fun createEnvironment(roundEnv: RoundEnvironment): Environment? {
        return try {
            Environment(processingEnv, roundEnv)
        } catch (e: Exception) {
            log.warn("Failed to start S0T. Please add s0t.yaml to your source path (e.g. src/main/java)")
            log.debug("Cause exception: ", e)
            null
        }
    }

    private fun buildModel(annotations: MutableSet<out TypeElement>) {
        annotations
            .sortedBy { ANNOTATION_ORDER[it::class.qualifiedName] }
            .forEach { processSingleAnnotation(it) }
    }

    private fun processSingleAnnotation(annotation: TypeElement) {
        when (annotation.qName()) {
            PlantUmlClass::class.qualifiedName -> annotatedElements(annotation)
                .map { TypeModel.of(it as TypeElement) }
                .forEach { model.addType(it) }

            PlantUmlExecutable.StartOfSequence::class.qualifiedName -> annotatedElements(annotation)
                .mapNotNull { ExecutableModel.of(it as ExecutableElement) }
                .forEach { model.addExecutable(it) }

            else -> log.error(
                """Unexpected annotation type: {}. Most likely there is a mismatch between the value of 
                       @SupportedAnnotationTypes and the annotation handling implemented in processAnnotation.""",
                annotation.qName()
            )
        }
    }

    private fun annotatedElements(annotation: TypeElement) = environment().roundEnvironment.getElementsAnnotatedWith(annotation)

    private fun renderOutput() {
        outputRenderers.forEach { it.render(model) }
    }

    companion object {
        private val log = LoggerFactory.getLogger(S0tProcessor::class.java.name)

        /**
         * Defines the order for processing annotations. This is used to make
         * sure that all annotations of a specific type (e.g. PlantUmlClass) are
         * processed before annotations with a lower priority, so that all model
         * elements on which other model elements depend are available at the
         * time of processing.
         */
        val ANNOTATION_ORDER: Map<String, Int> = mapOf(
            PlantUmlClass::class.qualifiedName!! to 0,
            PlantUmlExecutable.StartOfSequence::class.qualifiedName!! to 1
        )

        fun environment() = threadLocalEnv.get()!!
        fun configuration() = environment().configuration

        private val threadLocalEnv = ThreadLocal<Environment>()
    }

}