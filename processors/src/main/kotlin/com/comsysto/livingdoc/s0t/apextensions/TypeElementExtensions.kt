package com.comsysto.livingdoc.s0t.apextensions

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.model.TypeName
import javax.lang.model.element.TypeElement

/**
 * Get the type element's qualified name as a string.
 */
fun TypeElement.qName() = this.qualifiedName.toString()

/**
 * Get the type element's qualified name as a TypeName.
 */
fun TypeElement.typeName() = TypeName.parse(this.qualifiedName.toString())

/**
 * Checks if the type element is annotated with ``@PlantUmlClass``.
 */
fun isPlantUmlClass(type: TypeElement) = type.getAnnotation(PlantUmlClass::class.java) != null



