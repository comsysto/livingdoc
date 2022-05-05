package com.comsysto.livingdoc.s0t

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.model.TypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

fun TypeElement.qName() = this.qualifiedName.toString()
fun TypeElement.typeName() = TypeName.parse(this.qualifiedName.toString())

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

fun TypeMirror.typeArguments() = asDeclaredType()?.typeArguments ?: emptyList()

fun TypeMirror.asDeclaredType() = when (this) {
    is DeclaredType -> this
    else -> null
}

fun isPartOfDiagram(type: TypeMirror) = type.asTypeElement()?.getAnnotation(PlantUmlClass::class.java) != null
fun isPartOfDiagram(type: TypeElement) = type.getAnnotation(PlantUmlClass::class.java) != null



