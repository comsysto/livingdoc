package com.comsysto.livingdoc.annotation.processors;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.google.auto.service.AutoService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class PlantUmlClassProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        annotations.forEach(annotation -> processAnnotation(annotation, roundEnv));

        return true;
    }

    private void processAnnotation(final TypeElement annotation, final RoundEnvironment roundEnv) {
        final Set<? extends Element> annotated = roundEnv.getElementsAnnotatedWith(annotation);
        switch (annotation.getSimpleName().toString()) {
            case "PlantUmlClass":
                processClasses(annotated);
        }
    }

    private void processClasses(final Set<? extends Element> annotatedClassTypes) {
        final Map<DiagramId, List<ClassDiagramPart>> generatedFiles = annotatedClassTypes.stream()
            .map(TypeElement.class::cast)
            .map(this::createDiagramPart)
            .collect(groupingBy(ClassDiagramPart::getDiagramId));

        generatedFiles.keySet().forEach(id -> writeDiagram(id, generatedFiles.get(id)));
    }

    private ClassDiagramPart createDiagramPart(final TypeElement annotated) {
        final PlantUmlClass annotation = annotated.getAnnotation(PlantUmlClass.class);
        return new ClassDiagramPart(
            new DiagramId(annotation.diagramId()),
            annotated.getSimpleName(),
            String.format("class %s\n", annotated.getSimpleName()),
            generateAssociations(annotated));
    }

    private List<AssociationsPart> generateAssociations(final TypeElement annotated) {
        final Name className      = annotated.getSimpleName();
        final Name superClassName = ((DeclaredType) annotated.getSuperclass()).asElement().getSimpleName();
        return singletonList(new AssociationsPart(
            superClassName,
            className,
            String.format("%s <|-- %s", superClassName, className)));
    }

    private void writeDiagram(final DiagramId id, final List<ClassDiagramPart> parts) {
        final File   f                = new File(id.getValue() + ".puml");
        final String generatedDiagram = mergeDiagrams(parts);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
            out.write(generatedDiagram);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String mergeDiagrams(final List<ClassDiagramPart> parts) {
        Set<Name> whitelist = parts.stream()
            .map(ClassDiagramPart::getClassName)
            .collect(toSet());

        return "@startuml\n\n" +
               parts.stream()
                   .map(ClassDiagramPart::getGeneratedClassBody)
                   .collect(joining("\n")) +
               parts.stream()
                   .map(ClassDiagramPart::getAssociations)
                   .flatMap(List::stream)
                   .filter(association -> whitelist.contains(association.getLeft())
                                          && whitelist.contains(association.getRight()))
                   .map(AssociationsPart::getGenerated)
                   .collect(joining("\n")) +
               "\n\n@enduml";
    }

}