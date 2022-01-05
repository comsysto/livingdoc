package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.asTypeElement
import com.comsysto.livingdoc.s0t.model.Relation.*
import com.comsysto.livingdoc.s0t.typeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

/**
 * Models a language type.
 */
data class TypeModel(
    val name: TypeName.ComplexTypeName,
    val type: Type,
    val fields: List<FieldModel>,
    val realizations: List<Realization> = listOf(),
    val inheritance: Inheritance? = null,
    val associations: List<Association> = listOf(),
    val notes: List<Note> = listOf()
) {
    enum class Type {
        INTERFACE, ABSTRACT, CLASS, ENUM;

        companion object {
            fun of(typeElement: TypeElement): Type = when {
                typeElement.kind.isInterface -> INTERFACE
                typeElement.modifiers.contains(Modifier.ABSTRACT) -> ABSTRACT
                typeElement.kind == ElementKind.ENUM -> ENUM
                else -> CLASS
            }
        }
    }

    companion object {

        /**
         * Create a type model from a TypeElement.
         *
         * @param typeElement an annotation API type element
         * @return the type model.
         */
        fun of(typeElement: TypeElement) = TypeModel(
                typeElement.typeName() as TypeName.ComplexTypeName,
                Type.of(typeElement),
                FieldModel.allOf(typeElement),
                Realization.allOf(typeElement),
                Inheritance.of(typeElement),
                Association.allOf(typeElement),
                Note.allOf(typeElement))
    }
}