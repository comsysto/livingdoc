package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlDependency;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import lombok.Value;

@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
@PlantUmlNote(body = "Models an additional dependency relation")
@Value
public class AdditionalRelationPart implements RelationPart {

    @PlantUmlField
    RelationId id;

    @PlantUmlField
    TypePart source;

    @PlantUmlField
    PlantUmlDependency relation;
}
