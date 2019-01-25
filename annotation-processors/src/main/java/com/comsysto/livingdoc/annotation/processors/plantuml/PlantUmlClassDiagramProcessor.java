package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNotes;
import com.google.auto.service.AutoService;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
@Slf4j
public class PlantUmlClassDiagramProcessor extends AbstractProcessor {
    private final Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);

    public PlantUmlClassDiagramProcessor() {
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
        processPlantumlClass(roundEnv.getElementsAnnotatedWith(annotation)
                                 .stream()
                                 .map(TypeElement.class::cast)
                                 .collect(toSet()));
    }

    private void processPlantumlClass(final Set<TypeElement> annotatedTypes) {
        final Map<DiagramId, List<ClassDiagramPart>> generatedFiles = annotatedTypes.stream()
            .map(this::createDiagramPart)
            .flatMap(part -> part.getDiagramIds().stream()
                .map(id -> new ClassDiagramPart(
                    singleton(id),
                    part.getAnnotation(),
                    part.getTypeElement(),
                    part.getNotes())))
            .collect(groupingBy(part -> part.getDiagramIds().stream().findFirst().get()));

        generatedFiles.keySet().forEach(id -> createDiagram(id, generatedFiles.get(id)));
    }

    private ClassDiagramPart createDiagramPart(final TypeElement annotated) {
        final PlantUmlClass classAnnotation = annotated.getAnnotation(PlantUmlClass.class);

        return new ClassDiagramPart(
            stream(classAnnotation.diagramIds()).map(DiagramId::of).collect(toSet()),
            classAnnotation,
            annotated,
            getNoteAnnotations(annotated));
    }

    private List<PlantUmlNote> getNoteAnnotations(final TypeElement annotated) {
        return Optional.ofNullable(annotated.getAnnotation(PlantUmlNotes.class))
            .map(PlantUmlNotes::value)
            .map(Arrays::asList)
            .orElseGet(() -> Optional.ofNullable(annotated.getAnnotation(PlantUmlNote.class))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList()));
    }


    private void createDiagram(final DiagramId id, final List<ClassDiagramPart> parts) {
        final File settingsFile = new File(id.getValue() + "_class.properties");
        final File outFile      = new File(id.getValue() + "_class.puml");

        Properties settings = new Properties();
        try (final FileReader in = new FileReader(settingsFile)) {
            settings.load(in);
        } catch (IOException e) {
            log.debug("No settings file found: {}", settingsFile.getAbsoluteFile());
        }

        try (final BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
            final Template template = freemarkerConfiguration.getTemplate("class-diagram.puml.ftl");
            template.process(
                new ClassDiagram(
                    settings.getProperty("title", null),
                    stream(settings.getProperty("include.files", "").split(","))
                        .filter(StringUtils::isNoneBlank)
                        .collect(toList()),
                    parts),
                out);
        } catch (IOException | TemplateException e) {
            log.error("Failed", e);
            throw new RuntimeException(e);
        }
    }

}