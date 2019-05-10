package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import lombok.Value;

import javax.lang.model.element.TypeElement;

/**
 * A diagram part that represents a relation between two type elements.
 */
@Value
public class RelationPart {

    RelationId id;
    TypeElement left;
    TypeElement right;
    Relation relation;

    public enum Relation {

        /**
         * The right type realizes (implements) the left type.
         */
        REALIZATION,

        /**
         * The right type extends the left type.
         */
        INHERITANCE,

        /**
         * The left type references the right type in form of a directed
         * association.
         */
        ASSOCIATION
    }
}
