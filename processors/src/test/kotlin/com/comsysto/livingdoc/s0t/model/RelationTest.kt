package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType
import com.comsysto.livingdoc.s0t.model.Relation.*
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.annotatedClassTypeElement
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.fieldTypeElement
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.interfaceTypeElement
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.plantUmlFieldAnnotation
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.superClassTypeElement
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.typeElement
import com.comsysto.livingdoc.s0t.model.S0tModelTestObjectMother.variableElement
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import javax.lang.model.element.ElementKind
import javax.lang.model.type.DeclaredType

internal class RelationTest : BehaviorSpec({
    Given("A type element annotated with @PlantUmlClass") {

        When("I create an inheritance from a type element'") {
            val result = Inheritance.of(annotatedClassTypeElement)

            Then ("it should return the inheritance relation") {
                result shouldBe Inheritance(TypeRef.of(superClassTypeElement), TypeRef.of(annotatedClassTypeElement))
            }
        }

        When("I create realizations from a type element") {
            val result = Realization.allOf(annotatedClassTypeElement)

            Then("it should return a relation to the interface annotated with @PlantUmlClass") {
                result shouldBe listOf(Realization(TypeRef.of(interfaceTypeElement), TypeRef.of(annotatedClassTypeElement)))
            }
        }

        When("I create field associations from a type element") {
            val result = Association.allOf(annotatedClassTypeElement)

            Then("it should return the association annotated with @PlantUmlField") {
                val field = FieldModel("field1", TypeRef.of(fieldTypeElement), AccessModifier.PACKAGE)
                result shouldBe listOf(Association(TypeRef.of(annotatedClassTypeElement), field.type, field))
            }
        }

        When("I create field associations from a variable element with several properties") {
            val annotation = mockk<PlantUmlField>().apply {
                every { associationType } returns AssociationType.AGGREGATION
                every { sourceCardinality } returns "1"
                every { targetCardinality } returns "0..5"
                every { forceStandardTypeAssociation } returns false
            }

            val result = Association.allOf(variableElement("containerField", fieldTypeElement, annotation), annotatedClassTypeElement)

            Then("it should return a single association with the specified properties") {
                val field = FieldModel("containerField", TypeRef.of(fieldTypeElement), AccessModifier.PACKAGE)
                result shouldBe listOf(Association(TypeRef.of(annotatedClassTypeElement), field.type, field, "1", "0..5", AssociationType.AGGREGATION))
            }
        }

        When("I get the target types for a declared type") {
            val containerType = typeElement(
                "my.package.MyContainerType",
                ElementKind.CLASS,
                listOf(typeElement("1"), typeElement("2"), typeElement("1")).map { it.asType() as DeclaredType }
            )

            val result = Association.targetTypes(containerType.asType() as DeclaredType)

            Then("it should return a set containing every distinct type") {
                result shouldBe setOf(TypeRef.of(typeElement("1", elementKind = ElementKind.CLASS)), TypeRef.of(typeElement(
                    "2",
                    elementKind = ElementKind.CLASS
                )))
            }
        }

        When("I create field associations from a variable element with a container target type") {
            val typeArgs = listOf(typeElement("1"), typeElement("2")).map { it.asType() as DeclaredType }
            val t1 = TypeRef.of(typeArgs[0])
            val t2 = TypeRef.of(typeArgs[1])
            val fieldName = "field"
            val containerTypeElement = typeElement("containerField", ElementKind.CLASS, typeArgs)
            val field = variableElement(fieldName, containerTypeElement, plantUmlFieldAnnotation())
            val result = Association.allOf(field, annotatedClassTypeElement)

            Then("it should return a list of associations to each parameter types, but not to the container type itself") {
                val f = FieldModel(fieldName, TypeRef.of(containerTypeElement), AccessModifier.PACKAGE, listOf(t1, t2))

                result shouldBe listOf(
                    Association(TypeRef.of(annotatedClassTypeElement), t1, f),
                    Association(TypeRef.of(annotatedClassTypeElement), t2, f)
                )
            }
        }
    }
})
