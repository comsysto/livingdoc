package com.comsysto.livingdoc.kotlin.annotation.processors;

import com.google.auto.service.AutoService;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * Java wrapper for the Kotlin processor that makes it detectable by the
 * annotations framework.
 */
@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass")
@SupportedOptions(value = { "pumlgen.settings.dir", "pumlgen.out.dir" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class PlantUmlClassDiagramProcessor extends BasePlantUmlClassDiagramProcessor implements Processor {

    @Override
    public boolean process(
        @NotNull final Set<? extends TypeElement> annotations,
        @NotNull final RoundEnvironment roundEnv)
    {
        return super.process(annotations, roundEnv);
    }
}
