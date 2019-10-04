package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import lombok.Value;

/**
 * Used to identify a specific relation.
 */
@Value
@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
public class RelationId {
    String value;
}
