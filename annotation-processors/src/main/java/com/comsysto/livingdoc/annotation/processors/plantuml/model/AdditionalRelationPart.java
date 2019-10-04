package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlRelation;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import lombok.Value;

@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
@Value
public class AdditionalRelationPart implements RelationPart {

    @PlantUmlField
    RelationId id;

    @PlantUmlField
    TypePart source;

    @PlantUmlField
    PlantUmlRelation relation;
}
