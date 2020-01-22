package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static com.comsysto.livingdoc.annotation.processors.plantuml.Optionals.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlDependency;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlDependencies;
import com.comsysto.livingdoc.annotation.processors.plantuml.Optionals;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.IntrinsicRelationPart.Relation;
import lombok.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

/**
 * Models a type within a class diagram.
 */
@Value
@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
@PlantUmlNote(body = "Models a type in the diagram")
public class TypePart {

    /**
     * The IDs of the diagrams that should render this type part (please note
     * this may not be equal to the diagram IDs in the annotation).
     */
    @PlantUmlField
    private final Set<DiagramId> diagramIds;

    /**
     * The {@link PlantUmlClass} annotation attached to the type.
     */
    @PlantUmlField
    private final PlantUmlClass annotation;

    /**
     * The type element that models the type.
     */
    @PlantUmlField
    private final TypeElement typeElement;

    /**
     * A list of notes associated with this type.
     */
    @PlantUmlField
    private final List<PlantUmlNote> notes;

    @PlantUmlExecutable
    public String getName() {
        return typeElement.getSimpleName().toString();
    }

    /**
     * Get all relations independent of their type.
     *
     * @return all relations.
     */
    public List<IntrinsicRelationPart> getRelations() {
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
    @PlantUmlExecutable
    public List<IntrinsicRelationPart> getAssociations() {
        return getAnnotatedFields().stream()
            .filter(element -> element.getAnnotation(PlantUmlField.class).showAssociation())
            .flatMap(element -> stream(associationPart(element)))
            .collect(toList());
    }

    @PlantUmlExecutable
    public List<AdditionalRelationPart> getAdditionalRelations() {
        return getPlantUmlRelationAnnotations().stream()
            .map(annotation -> new AdditionalRelationPart(
                new RelationId(String.format("%s::%s", typeElement.getQualifiedName(), annotation.target())),
                this,
                annotation))
            .collect(toList());
    }

    private List<PlantUmlDependency> getPlantUmlRelationAnnotations() {
        return Optional.ofNullable(typeElement.getAnnotation(PlantUmlDependencies.class))
            .map(PlantUmlDependencies::value)
            .map(Arrays::asList)
            .orElseGet(() -> Optional.ofNullable(typeElement.getAnnotation(PlantUmlDependency.class))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList()));
    }

    /**
     * Get a list of variable elements representing the fields of the class that
     * are annotated with {@link PlantUmlField}.
     *
     * @return the list of variable elements.
     */
    @PlantUmlExecutable
    public List<VariableElement> getAnnotatedFields() {
        return typeElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(PlantUmlField.class) != null)
            .map(VariableElement.class::cast)
            .collect(toList());
    }

    /**
     * Get a list of executable elements representing the methods of the class
     * that are annotated with {@link PlantUmlExecutable}.
     *
     * @return the list of executable elements.
     */
    @PlantUmlExecutable
    public List<ExecutableElement> getAnnotatedMethods() {
        return typeElement.getEnclosedElements().stream()
            .filter(element -> element.getAnnotation(PlantUmlExecutable.class) != null)
            .map(ExecutableElement.class::cast)
            .collect(toList());
    }

    /**
     * Create the relation part for the specified variable element.
     *
     * @param field the variable element.
     *
     * @return the association relation part.
     */
    public Optional<IntrinsicRelationPart> associationPart(final VariableElement field) {
        final TypeMirror typeMirror = field.asType();
        final List<? extends TypeMirror> typeArguments = typeMirror.getKind() == TypeKind.DECLARED
                                                         ? ((DeclaredType) typeMirror).getTypeArguments()
                                                         : emptyList();
        return toTypeElement(typeMirror)
            .map(typeElement -> new IntrinsicRelationPart(
                new RelationId(field.getSimpleName().toString()),
                getTypeElement(),
                mainRelationType(typeElement, typeArguments),
                Relation.ASSOCIATION));
    }

    private TypeElement mainRelationType(
        final TypeElement typeElement,
        final List<? extends TypeMirror> typeArguments)
    {
        switch (typeElement.asType().getKind()) {
            case DECLARED:
                return typeArguments.stream().findFirst()
                    .flatMap(TypePart::toTypeElement)
                    .orElse(typeElement);
            case WILDCARD:
                final WildcardType wildcardType = (WildcardType) typeElement.asType();
                return toTypeElement(wildcardType.getExtendsBound() != null
                                     ? wildcardType.getExtendsBound()
                                     : wildcardType.getSuperBound()).orElse(typeElement);
            default:
                return typeElement;
        }
    }

    /**
     * Get the relation parts for all implemented interfaces.
     *
     * @return the realization parts.
     */
    public List<IntrinsicRelationPart> realizationParts() {
        return typeElement.getInterfaces().stream()
            .map(TypePart::toTypeElement)
            .flatMap(Optionals::stream)
            .map(parentElement -> new IntrinsicRelationPart(
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
    @PlantUmlExecutable
    public Optional<IntrinsicRelationPart> getSuperClassAssociation() {
        return Optional.ofNullable(getTypeElement().getSuperclass())
            .filter(DeclaredType.class::isInstance)
            .flatMap(TypePart::toTypeElement)
            .map(parentElement -> new IntrinsicRelationPart(
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
        return mirror instanceof DeclaredType && !mirror.getKind().isPrimitive()
               ? Optional.of((TypeElement) ((DeclaredType) mirror).asElement())
               : Optional.empty();
    }

    @PlantUmlExecutable
    public boolean isInterface() {
        return typeElement.getKind().isInterface();
    }

    @PlantUmlExecutable
    public boolean isAbstract() {
        return typeElement.getModifiers().contains(Modifier.ABSTRACT);
    }

    @PlantUmlExecutable
    public boolean isEnum() {
        return typeElement.getKind() == ElementKind.ENUM;
    }
}
