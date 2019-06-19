package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static java.util.stream.Collectors.toSet;

import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
public class SequenceDiagram {

    /**
     * the diagram title.
     */
    String title;

    /**
     * Any PlantUml files to be included.
     */
    List<String> includeFiles;

    /**
     * The executable parts to be rendered in this diagram.
     */
    List<ExecutablePart> parts;

    public Set<String> typeNames() {
        return parts.stream()
            .map(part -> part.getAnnotated().getEnclosingElement().asType())
            .map(ClassDiagram::simpleTypeName).collect(toSet());
    }
}