package com.comsysto.livingdoc.kotlin.annotation.processors.model

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror

/**
 * Models a type within a class diagram.
 */
class TypePart(

        /**
         * The [PlantUmlClass] annotation attached to the type.
         */
        val annotation: PlantUmlClass,

        /**
         * The type element that models the type.
         */
        val typeElement: TypeElement,

        /**
         * A list of notes associated with this type.
         */
        val notes: List<PlantUmlNote>
) {

    val name: String
        get() = typeElement.simpleName.toString()

    /**
     * Get all relations independent of their type.
     *
     * @return all relations.
     */
    val relations: List<RelationPart>
        get() = listOfNotNull(superClassAssociation) + realizationParts() + associations

    /**
     * Get all relations that are (field) associations.
     *
     * @return the associations.
     */
    val associations: List<RelationPart>
        get() = annotatedFields.flatMap { listOfNotNull(associationPart(it)) }

    /**
     * Get a list of variable elements representing the fields of the class that
     * are annotated with [PlantUmlField].
     *
     * @return the list of variable elements.
     */
    val annotatedFields: List<VariableElement>
        get() = typeElement.enclosedElements
                .filter { element -> element.getAnnotation(PlantUmlField::class.java) != null }
                .map { it as VariableElement }

    /**
     * Get the inheritance relation if there is a super class (other than
     * [Object]).
     *
     * @return the inheritance relation part or an empty optional if the super
     * class is [Object].
     */
    private val superClassAssociation: RelationPart?
        get() {
            val parentElement = toTypeElement(typeElement.superclass)
            return if (parentElement != null) RelationPart(
                    RelationId(parentElement.simpleName.toString()),
                    parentElement,
                    typeElement,
                    Relation.INHERITANCE)
            else null
        }

    val isInterface: Boolean
        get() = typeElement.kind.isInterface

    val isAbstract: Boolean
        get() = typeElement.modifiers.contains(Modifier.ABSTRACT)

    val isEnum: Boolean
        get() = typeElement.kind == ElementKind.ENUM

    /**
     * Create the relation part for the specified variable element.
     *
     * @param field the variable element.
     *
     * @return the association relation part.
     */
    private fun associationPart(field: VariableElement): RelationPart? {
        val fieldType = toTypeElement(field.asType())

        return if (fieldType != null)
            RelationPart(
                    RelationId(field.simpleName.toString()),
                    typeElement,
                    fieldType,
                    Relation.ASSOCIATION)
        else null
    }

    /**
     * Get the relation parts for all implemented interfaces.
     *
     * @return the realization parts.
     */
    private fun realizationParts(): List<RelationPart> {
        return typeElement.interfaces
                .filterIsInstance<DeclaredType>()
                .map { toTypeElement(it) }
                .mapNotNull { parentElement ->
                    if (parentElement != null)
                        RelationPart(
                                RelationId(parentElement.simpleName.toString()),
                                parentElement,
                                typeElement,
                                Relation.REALIZATION)
                    else null
                }
    }

    companion object {

        /**
         * Converts a type mirror to a type element if it does not represent a
         * primitive.
         *
         * @param mirror the type mirror.
         *
         * @return the element or an empty optional if the mirror represents a
         * primitive.
         */
        fun toTypeElement(mirror: TypeMirror): TypeElement? = when (mirror) {
            is DeclaredType -> mirror.asElement() as TypeElement
            else -> null
        }
    }
}