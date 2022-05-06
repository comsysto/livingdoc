package com.comsysto.livingdoc.s0t.model.relations

import com.comsysto.livingdoc.s0t.model.ImplicitlyFilterable
import com.comsysto.livingdoc.s0t.model.TypeModel
import com.comsysto.livingdoc.s0t.model.TypeName
import com.comsysto.livingdoc.s0t.model.TypeRef

const val KEY_AUTO_ADD_ASSOCIATIONS = "s0t.plantuml.class.relation.association.auto-add"
const val DEF_AUTO_ADD_ASSOCIATIONS = true

/**
 * Models a relation. Known relation types are **Realization** (e.g. a type
 * implementing an interface), **Inheritance** (a type inheriting from another)
 * and **Association** (a type referencing another via a field).
 */
interface Relation : ImplicitlyFilterable<Relation> {

    /**
     * The 'left' side of the relation. In case if Realization and Inheritance,
     * this is the parent type. In case of an Association, this is the type
     * owning the respective field declaration.
     */
    val left: TypeRef

    /**
     * The 'right' side of the relation. In case if Realization and Inheritance,
     * this is the child type. In case of an Association, this is the referenced
     * type.
     */
    val right: TypeRef

    override fun isPartOfDiagram(diagramId: String?, types: Map<TypeName.ComplexTypeName, TypeModel>) =
        types.containsKey(left.name) && types.containsKey(right.name)

    override fun filter(diagramId: String?, types: Map<TypeName.ComplexTypeName, TypeModel>) = this
}