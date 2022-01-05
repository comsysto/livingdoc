package com.comsysto.livingdoc.s0t.model

import org.apache.commons.lang3.StringUtils.substringAfterLast
import org.apache.commons.lang3.StringUtils.substringBeforeLast
import kotlin.reflect.KClass

/**
 * The name of a type.
 */
interface TypeName: Comparable<TypeName> {
    val packageName: String
    val simpleName: String
    fun asQualifiedName(): String

    override fun compareTo(other: TypeName): Int {
        return asQualifiedName().compareTo(other.asQualifiedName())
    }
    companion object {
        fun parse(fqn: String) = if (fqn.contains('.')) ComplexTypeName(substringBeforeLast(fqn, "."), substringAfterLast(fqn, "."))
            else SimpleTypeName(fqn)
    }

    /**
     * A simple unqualified type name.
     */
    data class SimpleTypeName(val name: String): TypeName {
        override val packageName: String
            get() = ""
        override val simpleName: String
            get() = name

        override fun asQualifiedName() = name
    }

    /**
     * A fully qualified type name.
     */
    data class ComplexTypeName (override val packageName: String, override val simpleName: String): TypeName {
        override fun asQualifiedName() = "${packageName}.${simpleName}"
    }
}
