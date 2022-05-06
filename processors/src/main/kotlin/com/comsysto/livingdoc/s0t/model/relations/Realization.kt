package com.comsysto.livingdoc.s0t.model.relations

import com.comsysto.livingdoc.s0t.apextensions.asTypeElement
import com.comsysto.livingdoc.s0t.apextensions.isPlantUmlClass
import com.comsysto.livingdoc.s0t.model.TypeRef
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * A Realization models an interface being implemented by a concrete type.
 *
 * @param interfaceType the implemented interface type.
 * @param implementingType the concrete type implementing the interface.
 */
data class Realization(val interfaceType: TypeRef, val implementingType: TypeRef) : BaseRelation(RelationId(interfaceType.name.asQualifiedName())) {
    override val left: TypeRef
        get() = interfaceType
    override val right: TypeRef
        get() = implementingType

    companion object {

        /**
         * Get all interfaces implemented by a type element.
         */
        fun allOf(typeElement: TypeElement): List<Realization> = typeElement.interfaces
            .filterIsInstance<DeclaredType>()
            .mapNotNull { it.asTypeElement() }
            .filter { isPlantUmlClass(it) }
            .map { interfaceElement -> Realization(interfaceType = TypeRef.Companion.of(interfaceElement), implementingType = TypeRef.of(typeElement)) }
    }
}