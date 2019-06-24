package com.comsysto.livingdoc.kotlin.annotation.processors.model

import javax.lang.model.element.TypeElement

/**
 * A diagram part that represents a relation between two type elements.
 */
data class RelationPart(val id: RelationId, val left: TypeElement, val right: TypeElement, val relation: Relation)

enum class Relation {

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
