package com.comsysto.livingdoc.s0t.model.relations

import com.comsysto.livingdoc.s0t.apextensions.asTypeElement
import com.comsysto.livingdoc.s0t.apextensions.isPlantUmlClass
import com.comsysto.livingdoc.s0t.model.TypeRef
import javax.lang.model.element.TypeElement

data class Inheritance(val superClassType: TypeRef, val implementingType: TypeRef) : BaseRelation(RelationId(superClassType.name.asQualifiedName())) {
    override val left: TypeRef
        get() = superClassType
    override val right: TypeRef
        get() = implementingType

    companion object {

        /**
         * Get the super class of a type element if it is present on the
         * diagram.
         */
        fun of(typeElement: TypeElement): Inheritance? = typeElement.superclass
            .asTypeElement()?.let {
                if (isPlantUmlClass(it))
                    Inheritance(superClassType = TypeRef.Companion.of(it), implementingType = TypeRef.of(typeElement))
                else null
            }
    }
}