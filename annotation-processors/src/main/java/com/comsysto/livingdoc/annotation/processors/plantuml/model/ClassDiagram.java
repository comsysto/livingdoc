package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static com.comsysto.livingdoc.annotation.processors.plantuml.model.IntrinsicRelationPart.Relation.ASSOCIATION;
import static com.comsysto.livingdoc.annotation.processors.plantuml.model.IntrinsicRelationPart.Relation.INHERITANCE;
import static com.comsysto.livingdoc.annotation.processors.plantuml.model.IntrinsicRelationPart.Relation.REALIZATION;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Position;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import freemarker.template.DefaultObjectWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

/**
 * Models a class diagram. The methods of this class are manly used by the
 * corresponding freemarker template (<code>class-diagram.puml.ftl</code>).
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@Data
@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
@PlantUmlNote(body = "Used as input for a freemarker template\nthat uses the provided fields and methods",
              position = Position.RIGHT)
public class ClassDiagram extends DefaultObjectWrapper {

    /**
     * the diagram title.
     */
    @PlantUmlField(showAssociation = false)
    private final String title;

    /**
     * Any PlantUml files to be included.
     */
    @PlantUmlField(showAssociation = false)
    private final List<String> includeFiles;

    /**
     * The type parts to be rendered in this diagram.
     */
    @PlantUmlField
    private final List<TypePart> parts;

    public String getTitle() {
        return Optional.ofNullable(title)
            .filter(StringUtils::isNoneBlank)
            .orElse(null);
    }

    /**
     * Get all inheritance or realization relations within this diagram.
     *
     * @return the inheritance relations.
     */
    @PlantUmlExecutable
    public List<IntrinsicRelationPart> getInheritanceRelations() {
        final Set<Name> whitelist = renderedTypeNames();

        return parts.stream()
            .map(TypePart::getRelations)
            .flatMap(List::stream)
            .filter(relation -> EnumSet.of(INHERITANCE, REALIZATION).contains(relation.getRelation()))
            .filter(relation -> shouldBeRendered(relation, whitelist))
            .collect(toList());
    }

    /**
     * Get all (field) associations within this diagram.
     *
     * @return the list of association relation parts.
     */
    @PlantUmlExecutable
    public List<IntrinsicRelationPart> getAssociations() {
        final Set<Name> whitelist = renderedTypeNames();

        return parts.stream()
            .map(TypePart::getRelations)
            .flatMap(List::stream)
            .filter(relation -> relation.getRelation() == ASSOCIATION)
            .filter(association -> shouldBeRendered(association, whitelist))
            .collect(toList());
    }

    /**
     * Get all additional relations within this diagram.
     *
     * @return the list of association relation parts.
     */
    @PlantUmlExecutable
    public List<AdditionalRelationPart> getAdditionalRelations() {
        final Set<String> whitelist = renderedTypeNames().stream()
            .map(Name::toString)
            .collect(toSet());

        return parts.stream()
            .map(TypePart::getAdditionalRelations)
            .flatMap(List::stream)
            .filter(part -> whitelist.contains(part.getRelation().target()))
            .collect(toList());
    }

    /**
     * Get the simple name of a type.
     *
     * @param typeMirror the type mirror.
     *
     * @return the simple name.
     */
    @PlantUmlExecutable
    public static String simpleTypeName(TypeMirror typeMirror) {
        final List<? extends TypeMirror> typeArguments = typeMirror.getKind() == TypeKind.DECLARED
                                                         ? ((DeclaredType) typeMirror).getTypeArguments()
                                                         : emptyList();
        switch (typeMirror.getKind()) {
            case DECLARED:
                return declaredTypeSimpleName((DeclaredType) typeMirror, typeArguments);
            case WILDCARD:
                return wildcardTypeSimpleName((WildcardType) typeMirror);
            default:
                return typeMirror.toString();
        }
    }

    private static String declaredTypeSimpleName(
        final DeclaredType typeMirror,
        final List<? extends TypeMirror> typeArguments)
    {
        return typeMirror.asElement().getSimpleName().toString()
               + (typeArguments.isEmpty() ? "" : typeParametersString(typeArguments));
    }

    private static String wildcardTypeSimpleName(final WildcardType typeMirror) {
        final TypeMirror extendsBound = typeMirror.getExtendsBound();
        final TypeMirror superBound = typeMirror.getSuperBound();
        return extendsBound != null ? String.format("? extends %s", simpleTypeName(extendsBound))
                                    : String.format("? super %s", simpleTypeName(superBound));
    }

    private static String typeParametersString(final List<? extends TypeMirror> typeArguments) {
        return typeArguments.stream()
            .map(ClassDiagram::simpleTypeName)
            .collect(joining(", ", "<", ">"));
    }

    /**
     * Get the names of all types to be rendered within this diagram.
     *
     * @return the list of type names.
     */
    private Set<Name> renderedTypeNames() {
        return parts.stream()
            .map(part -> part.getTypeElement().getSimpleName())
            .collect(toSet());
    }

    /**
     * Determine if a relation should be rendered in this diagram.
     *
     * @param intrinsicRelationPart the relation to be checked.
     * @param whitelist    the list of type names to be rendered in this diagram.
     *
     * @return true if the type should be rendered.
     */
    private boolean shouldBeRendered(final IntrinsicRelationPart intrinsicRelationPart, final Set<Name> whitelist) {
        return whitelist.stream().anyMatch(name -> name.contentEquals(intrinsicRelationPart.getRight().getSimpleName()))
               && whitelist.stream().anyMatch(name -> name.contentEquals(intrinsicRelationPart.getLeft().getSimpleName()));
    }
}
