package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.apextensions.typeName
import com.comsysto.livingdoc.s0t.model.relations.Association
import com.comsysto.livingdoc.s0t.model.relations.Dependency
import com.comsysto.livingdoc.s0t.model.relations.Inheritance
import com.comsysto.livingdoc.s0t.model.relations.Realization
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Models a language type.
 */
data class TypeModel(
    val name: TypeName.ComplexTypeName,
    val type: Type,
    val fields: List<FieldModel>,
    val realizations: List<Realization> = listOf(),
    val inheritance: Inheritance? = null,
    val associations: List<Association> = listOf(),
    val dependencies: List<Dependency> = listOf(),
    val notes: List<NoteModel> = listOf(),
    override val diagramIds: Set<String> = setOf()
) : ExplicitlyFilterable<TypeModel> {
    enum class Type {
        INTERFACE, ABSTRACT, CLASS, ENUM;

        companion object {
            fun of(typeElement: TypeElement): Type = when {
                typeElement.kind.isInterface -> INTERFACE
                typeElement.modifiers.contains(Modifier.ABSTRACT) -> ABSTRACT
                typeElement.kind == ElementKind.ENUM -> ENUM
                else -> CLASS
            }
        }
    }

    companion object {

        /**
         * Create a type model from a TypeElement.
         *
         * @param typeElement an annotation API type element
         * @return the type model.
         */
        fun of(typeElement: TypeElement) = TypeModel(
            typeElement.typeName() as TypeName.ComplexTypeName,
            Type.of(typeElement),
            FieldModel.allOf(typeElement),
            Realization.allOf(typeElement),
            Inheritance.of(typeElement),
            Association.allOf(typeElement),
            Dependency.allOf(typeElement),
            NoteModel.allOf(typeElement),
            typeElement.getAnnotation(PlantUmlClass::class.java).diagramIds.toSet()
        )
    }

    override fun filter(diagramId: String?, types: Map<TypeName.ComplexTypeName, TypeModel>) = TypeModel(
        name,
        type,
        fields,
        realizations.filter { it.isPartOfDiagram(diagramId, types) },
        inheritance?.let { if (it.isPartOfDiagram(diagramId, types)) it else null },
        associations.filter { it.isPartOfDiagram(diagramId, types) },
        dependencies.filter { it.isPartOfDiagram(diagramId, types) },
        notes.filter { it.isPartOfDiagram(diagramId) })
}