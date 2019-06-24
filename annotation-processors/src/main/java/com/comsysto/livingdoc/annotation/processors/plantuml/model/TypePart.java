package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static com.comsysto.livingdoc.annotation.processors.plantuml.Optionals.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.processors.plantuml.Optionals;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.RelationPart.Relation;
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

/**
 * Models a type within a class diagram.
 */
@Value
public class TypePart {

    /**
     * The IDs of the diagrams that should render this type part (please note
     * this may not be equal to the diagram IDs in the annotation).
     */
    Set<DiagramId> diagramIds;

    /**
     * The {@link PlantUmlClass} annotation attached to the type.
     */
    PlantUmlClass annotation;

    /**
     * The type element that models the type.
     */
    TypeElement typeElement;

    /**
     * A list of notes associated with this type.
     */
    List<PlantUmlNote> notes;

    public String getName() {
        return typeElement.getSimpleName().toString();
    }

    /**
     * Get all relations independent of their type.
     *
     * @return all relations.
     */
    public List<RelationPart> getRelations() {
        return concat(
            concat(
                stream(getSuperClassAssociation()),
                realizationParts().stream()),
            getAssociations().stream())
            .collect(toList());
    }

    /**
     * Get all relations that are (field) associations.
     *
     * @return the associations.
     */
    public List<RelationPart> getAssociations() {
        return getAnnotatedFields().stream()
            .flatMap(element -> Optionals.stream(associationPart(element)))
            .collect(toList());
    }

    /**
     * Get a list of variable elements representing the fields of the class that
     * are annotated with {@link PlantUmlField}.
     *
     * @return the list of variable elements.
     */
    public List<VariableElement> getAnnotatedFields() {
        return typeElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(PlantUmlField.class) != null)
            .map(VariableElement.class::cast)
            .collect(toList());
    }

    /**
     * Create the relation part for the specified variable element.
     *
     * @param field the variable element.
     *
     * @return the association relation part.
     */
    public Optional<RelationPart> associationPart(final VariableElement field) {
        return toTypeElement(field.asType())
            .map(typeElement -> new RelationPart(
                new RelationId(field.getSimpleName().toString()),
                getTypeElement(),
                typeElement,
                Relation.ASSOCIATION));
    }

    /**
     * Get the relation parts for all implemented interfaces.
     *
     * @return the realization parts.
     */
    public List<RelationPart> realizationParts() {
        return typeElement.getInterfaces().stream()
            .map(TypePart::toTypeElement)
            .flatMap(Optionals::stream)
            .map(parentElement -> new RelationPart(
                new RelationId(parentElement.getSimpleName().toString()),
                parentElement,
                getTypeElement(),
                Relation.REALIZATION))
            .collect(toList());
    }

    /**
     * Get the inheritance relation if there is a super class (other than
     * {@link Object}).
     *
     * @return the inheritance relation part or an empty optional if the super
     * class is {@link Object}.
     */
    public Optional<RelationPart> getSuperClassAssociation() {
        return Optional.ofNullable(getTypeElement().getSuperclass())
            .filter(DeclaredType.class::isInstance)
            .flatMap(TypePart::toTypeElement)
            .map(parentElement -> new RelationPart(
                new RelationId(parentElement.getSimpleName().toString()),
                parentElement,
                getTypeElement(),
                Relation.INHERITANCE));
    }

    /**
     * Converts a type mirror to a type element if it does not represent a
     * primitive.
     *
     * @param mirror the type mirror.
     *
     * @return the element or an empty optional if the mirror represents a
     * primitive.
     */
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
