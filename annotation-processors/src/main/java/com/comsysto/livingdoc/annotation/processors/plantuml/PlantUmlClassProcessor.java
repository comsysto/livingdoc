package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Container;
import com.google.auto.service.AutoService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@Slf4j
public class PlantUmlClassProcessor extends AbstractProcessor {
    private final Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);

    public PlantUmlClassProcessor() {
        freemarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(
            this.getClass(),
            "/com/comsysto/livingdoc/annotation/processors/plantuml"));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> processAnnotation(annotation, roundEnv));
        return true;
    }

    private void processAnnotation(final TypeElement annotation, final RoundEnvironment roundEnv) {
        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(annotation);
        switch (annotation.getSimpleName().toString()) {
            case "PlantUmlClass":
                processPlantumlClass(annotated.stream().map(TypeElement.class::cast).collect(toSet()));
                break;
        }
    }

    private void processPlantumlClass(final Set<TypeElement> annotatedTypes) {
        final Map<DiagramId, List<ClassDiagramPart>> generatedFiles = annotatedTypes.stream()
            .map(this::createDiagramPart)
            .collect(groupingBy(ClassDiagramPart::getDiagramId));

        generatedFiles.keySet().forEach(id -> createDiagram(id, generatedFiles.get(id)));
    }

    private ClassDiagramPart createDiagramPart(final TypeElement annotated) {
        final PlantUmlClass classAnnotation = annotated.getAnnotation(PlantUmlClass.class);

        return ClassDiagramPart.builder()
            .diagramId(new DiagramId(classAnnotation.diagramId()))
            .annotation(classAnnotation)
            .typeElement(annotated)
            .notes(getNotes(annotated))
            .build();
    }

    private List<PlantUmlNote> getNotes(final TypeElement annotated) {
        return Optional.ofNullable(annotated.getAnnotation(Container.class))
            .map(Container::value)
            .map(Arrays::asList)
            .orElseGet(() -> Optional.ofNullable(annotated.getAnnotation(PlantUmlNote.class))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList()));
    }

    private void createDiagram(final DiagramId id, final List<ClassDiagramPart> parts) {
        final File f = new File(id.getValue() + "_class.puml");

        try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
            final Template template = freemarkerConfiguration.getTemplate("class-diagram.puml.ftl");
            template.process(new ClassDiagram(parts), out);
        } catch (IOException | TemplateException e) {
            log.error("Failed", e);
            throw new RuntimeException(e);
        }
    }

}