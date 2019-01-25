package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import freemarker.template.DefaultObjectWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.Name;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClassDiagram extends DefaultObjectWrapper {

    private final List<ClassDiagramPart> parts;

    public List<String> getIncludeFiles() {
        return parts.stream()
            .flatMap(part -> stream(part.getAnnotation().includeFiles()))
            .collect(toList());
    }

    public List<AssociationsPart> getInheritanceAssociations() {
        Set<Name> whitelist = parts.stream()
            .map(part -> part.getTypeElement().getSimpleName())
            .collect(toSet());

        return parts.stream()
            .map(ClassDiagramPart::getSuperClassAssociation)
            .flatMap(ClassDiagram::toStream)
            .filter(association -> whitelist.contains(association.getLeft().getSimpleName()))
            .collect(toList());
    }

    private static <T> Stream<T> toStream(final Optional<T> t) {
        return t.map(Stream::of).orElse(Stream.empty());
    }
}
