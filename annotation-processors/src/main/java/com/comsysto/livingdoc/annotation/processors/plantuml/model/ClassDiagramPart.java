package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static com.comsysto.livingdoc.annotation.processors.plantuml.Optionals.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.processors.plantuml.Optionals;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.AssociationPart.Relation;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@Value
public class ClassDiagramPart {
    private final Set<DiagramId> diagramIds;
    private final PlantUmlClass annotation;
    private final TypeElement typeElement;
    private final List<PlantUmlNote> notes;

    public String getName() {
        return typeElement.getSimpleName().toString();
    }

    public List<AssociationPart> getAssociations() {
        return concat(
            concat(
                stream(getSuperClassAssociation()),
                getInterfaceAssociations().stream()),
            getReferenceAssociations().stream())
            .collect(toList());
    }

    public List<AssociationPart> getReferenceAssociations() {
        return typeElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(PlantUmlField.class) != null)
            .flatMap(element -> Optionals.stream(referenceAssociation((VariableElement) element)))
            .collect(toList());
    }

    private Optional<AssociationPart> referenceAssociation(final VariableElement field) {
        return toTypeElement(field.asType())
            .map(typeElement -> new AssociationPart(
                new AssociationId(field.getSimpleName().toString()),
                getTypeElement(),
                typeElement,
                Relation.REFERENCES));
    }

    public List<AssociationPart> getInterfaceAssociations() {
        return typeElement.getInterfaces().stream()
            .map(ClassDiagramPart::toTypeElement)
            .flatMap(Optionals::stream)
            .map(parentElement -> new AssociationPart(
                new AssociationId(parentElement.getSimpleName().toString()),
                parentElement,
                getTypeElement(),
                Relation.IMPLEMENTS))
            .collect(toList());
    }

    public Optional<AssociationPart> getSuperClassAssociation() {
        return Optional.ofNullable(getTypeElement().getSuperclass())
            .filter(DeclaredType.class::isInstance)
            .flatMap(ClassDiagramPart::toTypeElement)
            .map(parentElement -> new AssociationPart(
                new AssociationId(parentElement.getSimpleName().toString()),
                parentElement,
                getTypeElement(),
                Relation.EXTENDS));
    }

    public static Optional<TypeElement> toTypeElement(TypeMirror mirror) {
        return mirror.getKind().isPrimitive()
               ? Optional.empty()
               : Optional.of((TypeElement) ((DeclaredType) mirror).asElement());
    }

    public boolean isInterface() {
        return typeElement.getKind().isInterface();
    }

    public boolean isAbstract() {
        return typeElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    public boolean isEnum() {
        return typeElement.getKind() == ElementKind.ENUM;
    }
}
