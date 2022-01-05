package com.comsysto.livingdoc.s0t.model

/**
 * The fully-qualified name of an executable.
 */
data class ExecutableName(val typeName: TypeName.ComplexTypeName, val name: String) : Comparable<ExecutableName> {
    override fun compareTo(other: ExecutableName): Int {
        val typeResult = typeName.compareTo(other.typeName)
        return if (typeResult != 0) typeResult else name.compareTo(other.name)
    }
}