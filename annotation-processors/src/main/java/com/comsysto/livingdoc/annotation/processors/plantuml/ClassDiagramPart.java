package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.stream.Collectors.toList;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;
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

    public List<String> getInterfaceNames() {
        return typeElement.getInterfaces().stream()
            .map(ClassDiagramPart::toTypeElement)
            .map(TypeElement::getSimpleName)
            .map(Object::toString)
            .collect(toList());
    }

    Optional<AssociationsPart> getSuperClassAssociation() {
        return Optional.ofNullable(getTypeElement().getSuperclass())
            .filter(DeclaredType.class::isInstance)
            .map(ClassDiagramPart::toTypeElement)
            .map(parentElement -> new AssociationsPart(parentElement, getTypeElement()));
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
