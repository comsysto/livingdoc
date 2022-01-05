package com.comsysto.livingdoc.s0t

import com.comsysto.livingdoc.s0t.model.TypeName
import com.comsysto.livingdoc.s0t.model.TypeRef
import io.mockk.every
import io.mockk.mockk
import javax.lang.model.element.Name
import kotlin.reflect.KClass

object TestUtils {
    fun typeRef(type: KClass<*>) = typeRef(typeName(type))
    fun typeRef(typeName: TypeName) = TypeRef(typeName, TypeRef.Kind.COMPLEX)
    fun typeName(type: KClass<*>) = TypeName.parse(type.java.name)

    fun name(s: String): Name {
        val name = mockk<Name>()
        every { name.toString() } returns s
        return name
    }
}