package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import freemarker.template.DefaultObjectWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;
import javax.lang.model.element.Name;

@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@Data
public class ClassDiagram extends DefaultObjectWrapper {

    private final String title;
    private final List<String> includeFiles;
    private final List<ClassDiagramPart> parts;

    public List<AssociationPart> getInheritanceAssociations() {
        final Set<Name> whitelist = parts.stream()
            .map(part -> part.getTypeElement().getSimpleName())
            .collect(toSet());

        return parts.stream()
            .map(ClassDiagramPart::getAssociations)
            .flatMap(List::stream)
            .filter(association -> whitelist.contains(association.getLeft().getSimpleName()))
            .collect(toList());
    }
}
