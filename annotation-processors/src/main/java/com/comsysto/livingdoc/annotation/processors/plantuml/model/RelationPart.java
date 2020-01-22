package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;

@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
public interface RelationPart {

    @PlantUmlExecutable
    RelationId getId();
}
