package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.TestUtils.name
import com.comsysto.livingdoc.s0t.annotation.plantuml.*
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType
import com.comsysto.livingdoc.s0t.asTypeElement
import io.mockk.every
import io.mockk.mockk
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind

object S0tModelTestObjectMother {
    val superClassTypeName = name("my.package.name.MySuperClass")

    val plantUmlClassAnnotation = mockk<PlantUmlClass>().apply {
        every { autoCreateFields } returns AutoCreateType.NO
        every { autoCreateAssociations } returns AutoCreateType.NO
    }

    val superClassTypeElement = mockk<TypeElement>().apply {
        every { qualifiedName } returns superClassTypeName
        every { getAnnotation(PlantUmlClass::class.java) } returns plantUmlClassAnnotation
        every { kind } returns ElementKind.CLASS
    }

    val superClassType = mockk<DeclaredType>().apply {
        every { asTypeElement() } returns superClassTypeElement
    }

    const val interfaceTypeName = "my.package.MyInterface"
    val interfaceTypeElement = mockInterfaceTypeElement(interfaceTypeName, mockk())
    val notAnnotatedInterfaceType =
        mockk<DeclaredType>().apply { every { asTypeElement() } returns mockInterfaceTypeElement("some.other.InterfaceNotAnnotated") }
    val annotatedInterfaceType = mockk<DeclaredType>().apply { every { asTypeElement() } returns interfaceTypeElement }

    fun plantUmlFieldAnnotation(
        pShowAssociation: Boolean = true,
        pForceStandardTypeAssociation: Boolean = false,
        pSourceCardinality: String = "",
        pTargetCardinality: String = "",
        pAssociationType: AssociationType = AssociationType.STANDARD
    ) = mockk<PlantUmlField>().apply {
        every { showAssociation } returns pShowAssociation
        every { forceStandardTypeAssociation } returns pForceStandardTypeAssociation
        every { sourceCardinality } returns pSourceCardinality
        every { targetCardinality } returns pTargetCardinality
        every { associationType } returns pAssociationType
    }

    val annotatedClassTypeElement = typeElement("my.package.name.MyType").apply {
        every { superclass } returns superClassType
        every { interfaces } returns listOf(notAnnotatedInterfaceType, annotatedInterfaceType)
        every { enclosedElements } returns listOf(
            variableElement("field1", typeElement("my.package.MyFieldType"), plantUmlFieldAnnotation()),
            variableElement("field2", typeElement("my.package.MyFieldType")),
            constructorElement,
            methodElement
        )
        every { getAnnotation(PlantUmlClass::class.java) } returns plantUmlClassAnnotation
        every { getAnnotation(PlantUmlNotes::class.java) } returns mockPlantUmlNotes(note1, note2)
    }

    val constructorElement = mockk<ExecutableElement>()
    val methodElement = mockk<ExecutableElement>()
    val fieldTypeElement = typeElement("my.package.MyFieldType")

    /**
     * Creates a complex type element together with its type mirror.
     *
     * @param name the name of the type.
     * @param typeArgs a list of type arguments.
     *
     * @return the type element.
     */
    fun typeElement(
        name: String,
        elementKind: ElementKind = ElementKind.CLASS,
        typeArgs: List<DeclaredType> = emptyList(),
        annotation: PlantUmlClass? = plantUmlClassAnnotation
    ): TypeElement {
        val typeElement = mockk<TypeElement>()

        val typeMirror: DeclaredType = mockk<DeclaredType>().apply {
            every { kind } returns TypeKind.DECLARED
            every { typeArguments } returns typeArgs
        }

        every { typeMirror.asTypeElement() } returns typeElement
        typeElement.apply {
            every { qualifiedName } returns name(name)
            every { asType() } returns typeMirror
            every { kind } returns elementKind
            every { getAnnotation(PlantUmlClass::class.java) } returns annotation
        }
        return typeElement
    }

    val fieldElement = variableElement("field", typeElement("my.package.MyFieldType"), null)

    fun variableElement(name: String, typeElement: TypeElement, annotation: PlantUmlField? = null) = mockk<VariableElement>().apply {
        every { simpleName } returns name(name)
        every { asType() } returns typeElement.asType()
        annotation.apply { every { getAnnotation(PlantUmlField::class.java) } returns annotation }
        every { modifiers } returns setOf(Modifier.DEFAULT)
    }

    val annotatedFieldElement = variableElement("myField", fieldTypeElement, plantUmlFieldAnnotation())

    val note1 = mockPlantUmlNote("A note", Position.RIGHT)
    val note2 = mockPlantUmlNote("Another note", Position.TOP)

    fun mockPlantUmlNotes(vararg plantUmlNotes: PlantUmlNote): PlantUmlNotes {
        val notesAnnotation = mockk<PlantUmlNotes>()
        every { notesAnnotation.value } returns plantUmlNotes.map { it }.toTypedArray()
        return notesAnnotation
    }

    fun mockPlantUmlNote(body: String, position: Position): PlantUmlNote {
        val plantUmlNote = mockk<PlantUmlNote>()
        every { plantUmlNote.value } returns body
        every { plantUmlNote.position } returns com.comsysto.livingdoc.s0t.annotation.plantuml.Position.valueOf(position.name)
        return plantUmlNote
    }

    fun mockInterfaceTypeElement(interfaceTypeName: String, plantUmlClassAnnotation: PlantUmlClass? = null) = mockk<TypeElement> {
        every { qualifiedName } returns name(interfaceTypeName)
        every { getAnnotation(PlantUmlClass::class.java) } returns plantUmlClassAnnotation
        every { kind } returns ElementKind.INTERFACE
    }

}
