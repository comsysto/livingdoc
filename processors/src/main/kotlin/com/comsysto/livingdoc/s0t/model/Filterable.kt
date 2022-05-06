package com.comsysto.livingdoc.s0t.model

/**
 * Marks a model element that may or may not show up in a diagram dependent on
 * its ID.
 */
interface Filterable<out T: Filterable<T>> {

    /**
     * Returns an instance of the element with all its sub-elements filtered by
     * diagram ID.
     */
    fun filter(diagramId: String?, types: Map<TypeName.ComplexTypeName, TypeModel>): T
}

interface ExplicitlyFilterable<out T : ExplicitlyFilterable<T>> : Filterable<T> {

    val diagramIds: Set<String>

    fun isPartOfDiagram(diagramId: String?) =
        if (diagramId != null) diagramIds.contains(diagramId)
        else !diagramIds.contains("!default")
}

interface ImplicitlyFilterable<out T : ImplicitlyFilterable<T>> : Filterable<T> {

    fun isPartOfDiagram(diagramId: String?, types: Map<TypeName.ComplexTypeName, TypeModel>): Boolean
}

