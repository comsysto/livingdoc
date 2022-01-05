package com.comsysto.livingdoc.s0t

import com.comsysto.livingdoc.s0t.model.TypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass

fun TypeElement.qName() = this.qualifiedName.toString()
fun TypeElement.typeName() = TypeName.parse(this.qualifiedName.toString())

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

fun TypeMirror.asDeclaredType() = when (this) {
    is DeclaredType -> this
    else -> null
}
