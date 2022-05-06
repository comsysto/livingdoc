package com.comsysto.livingdoc.s0t.apextensions

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * Checks if the type element represents an enum type.
 */
fun TypeMirror.isEnum() = this.asTypeElement()?.kind != ElementKind.ENUM

/**
 * Converts a type mirror to a type element if it does not represent a
 * primitive.
 *
 * @param mirror the type mirror.
 *
 * @return the element or an empty optional if the mirror represents a
 * primitive.
 */
fun TypeMirror.asTypeElement(): TypeElement? = when (this) {
    is DeclaredType -> this.asElement() as TypeElement
    else -> null
}

/**
 * Get the type mirror's type arguments.
 */
fun TypeMirror.typeArguments() = asDeclaredType()?.typeArguments ?: emptyList()

/**
 * Converts the type mirror to a DeclaredType if possible.
 */
fun TypeMirror.asDeclaredType() = when (this) {
    is DeclaredType -> this
    else -> null
}

/**
 * Checks if the type mirror is annotated with ``@PlantUmlClass``.
 */
fun isPlantUmlClass(type: TypeMirror) = type.asTypeElement()?.getAnnotation(PlantUmlClass::class.java) != null