package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import lombok.NonNull;
import lombok.Value;

/**
 * The unique ID of a diagram.
 */
@Value
@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
public class DiagramId {
    private static final DiagramId DEFAULT = new DiagramId("package");
    private final String value;

    private DiagramId(final String value) {
        this.value = value;
    }

    public static DiagramId of(@NonNull final String s) {
        return s.equals(DEFAULT.value) ? DEFAULT : new DiagramId(s);
    }
}
