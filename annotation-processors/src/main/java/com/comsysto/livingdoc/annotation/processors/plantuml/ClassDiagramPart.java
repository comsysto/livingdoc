package com.comsysto.livingdoc.annotation.processors.plantuml;

import static com.comsysto.livingdoc.annotation.processors.plantuml.Optionals.stream;
import static java.util.stream.Collectors.toList;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.processors.plantuml.AssociationPart.Relation;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@Value
@Builder
public class ClassDiagramPart {
    private final DiagramId diagramId;
    private final PlantUmlClass annotation;
    private final TypeElement typeElement;
    private final List<PlantUmlNote> notes;

    public String getName() {
        return typeElement.getSimpleName().toString();
    }

    public List<AssociationPart> getAssociations() {
        return Stream.concat(stream(getSuperClassAssociation()), getInterfaceAssociations().stream())
            .collect(toList());
    }

    public List<AssociationPart> getInterfaceAssociations() {
        return typeElement.getInterfaces().stream()
            .map(ClassDiagramPart::toTypeElement)
            .map(parentElement -> new AssociationPart(parentElement, getTypeElement(), Relation.IMPLEMENTS))
            .collect(toList());
    }

    public Optional<AssociationPart> getSuperClassAssociation() {
        return Optional.ofNullable(getTypeElement().getSuperclass())
            .filter(DeclaredType.class::isInstance)
            .map(ClassDiagramPart::toTypeElement)
            .map(parentElement -> new AssociationPart(parentElement, getTypeElement(), Relation.EXTENDS));
    }

    public static TypeElement toTypeElement(TypeMirror mirror) {
        return (TypeElement) ((DeclaredType) mirror).asElement();
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
