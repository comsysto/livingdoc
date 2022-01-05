package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.model.TypeName.ComplexTypeName
import java.util.*

/**
 * This is the root of SoT's data model. It is built step by step by all
 * participating annotation processors and, at the end of processing, contains
 * the accumulated results.
 */
class S0tModel {
    val types = TreeMap<ComplexTypeName, TypeModel>()
    val executables = TreeMap<ExecutableName, ExecutableModel>()

    fun addType(type: TypeModel) {
        types[type.name] = type
    }

    fun addExecutable(executable: ExecutableModel) {
        executables[executable.name] = executable
    }
}