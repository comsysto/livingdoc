package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.asTypeElement
import com.comsysto.livingdoc.s0t.typeName
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind.*
import javax.lang.model.type.TypeMirror

/**
 * Models the _reference_ to a type that is possibly not yet known (e.g. because
 * it hasn't been visited yet by the annotation processor).
 */
data class TypeRef(val name: TypeName, val kind: Kind) {

    companion object {
        fun of(type: TypeMirror): TypeRef = when(type.kind) {
            BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE -> TypeRef(TypeName.SimpleTypeName(type.toString()), Kind.PRIMITIVE)
            DECLARED -> of(type.asTypeElement()!!)
            else -> TypeRef(TypeName.SimpleTypeName(type.toString()), Kind.UNKNOWN)
        }
        fun of(typeElement: TypeElement): TypeRef = TypeRef(typeElement.typeName(), Kind.COMPLEX)
    }

    enum class Kind {
        PRIMITIVE, COMPLEX, UNKNOWN
    }
}