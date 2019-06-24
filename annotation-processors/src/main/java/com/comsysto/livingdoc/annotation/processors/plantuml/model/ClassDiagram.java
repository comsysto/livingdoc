package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static com.comsysto.livingdoc.annotation.processors.plantuml.model.RelationPart.Relation.ASSOCIATION;
import static com.comsysto.livingdoc.annotation.processors.plantuml.model.RelationPart.Relation.INHERITANCE;
import static com.comsysto.livingdoc.annotation.processors.plantuml.model.RelationPart.Relation.REALIZATION;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import freemarker.template.DefaultObjectWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * Models a class diagram. The methods of this class are manly used by the
 * corresponding freemarker template (<code>class-diagram.puml.ftl</code>).
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@Data
public class ClassDiagram extends DefaultObjectWrapper {

    /**
     * the diagram title.
     */
    private final String title;

    /**
     * Any PlantUml files to be included.
     */
    private final List<String> includeFiles;

    /**
     * The type parts to be rendered in this diagram.
     */
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
    public List<RelationPart> getInheritanceRelations() {
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
    public List<RelationPart> getAssociations() {
        final Set<Name> whitelist = renderedTypeNames();

        return parts.stream()
            .map(TypePart::getRelations)
            .flatMap(List::stream)
            .filter(relation -> relation.getRelation() == ASSOCIATION)
            .filter(association -> shouldBeRendered(association, whitelist))
            .collect(toList());
    }

    /**
     * Get the simple name of a type.
     *
     * @param typeMirror the type mirror.
     *
     * @return the simple name.
     */
    public static String simpleTypeName(TypeMirror typeMirror) {
        return typeMirror.toString().contains(".")
               ? substringAfterLast(typeMirror.toString(), ".")
               : typeMirror.toString();
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
     * @param relationPart the relation to be checked.
     * @param whitelist    the list of type names to be rendered in this diagram.
     *
     * @return true if the type should be rendered.
     */
    private boolean shouldBeRendered(final RelationPart relationPart, final Set<Name> whitelist) {
        return whitelist.stream().anyMatch(name -> name.contentEquals(relationPart.getRight().getSimpleName()))
               && whitelist.stream().anyMatch(name -> name.contentEquals(relationPart.getLeft().getSimpleName()));
    }
}
