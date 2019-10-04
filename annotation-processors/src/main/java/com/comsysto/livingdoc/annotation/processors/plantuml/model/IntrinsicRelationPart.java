package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor;
import lombok.Value;

import javax.lang.model.element.TypeElement;

/**
 * A diagram part that represents a relation between two type elements.
 */
@Value
@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
public class IntrinsicRelationPart implements RelationPart {


    @PlantUmlField
    RelationId id;

    @PlantUmlField
    TypeElement left;

    @PlantUmlField
    TypeElement right;

    @PlantUmlField
    Relation relation;

    @PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
    public enum Relation {

        /**
         * The right type realizes (implements) the left type.
         */
        @PlantUmlField(showAssociation = false)
        REALIZATION,

        /**
         * The right type extends the left type.
         */
        @PlantUmlField(showAssociation = false)
        INHERITANCE,

        /**
         * The left type references the right type in form of a directed
         * association.
         */
        @PlantUmlField(showAssociation = false)
        ASSOCIATION
    }
}
