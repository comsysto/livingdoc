package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.S0tProcessor.Companion.configuration
import com.comsysto.livingdoc.s0t.annotation.plantuml.AutoCreateType
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType.STANDARD
import com.comsysto.livingdoc.s0t.asDeclaredType
import com.comsysto.livingdoc.s0t.asTypeElement
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

private const val KEY_AUTO_ADD_ASSOCIATIONS = "s0t.plantuml.class.relation.association.auto-add"
private const val DEF_AUTO_ADD_ASSOCIATIONS = true

/**
 * Models a relation. Known relation types are **Realization** (e.g. a type
 * implementing an interface), **Inheritance** (a type inheriting from another)
 * and **Association** (a type referencing another via a field).
 */
interface Relation {

    val left: TypeRef
    val right: TypeRef

    abstract class BaseRelation(val id: RelationId) : Relation

    data class Realization(val interfaceType: TypeRef, val implementingType: TypeRef) : BaseRelation(RelationId(interfaceType.name.asQualifiedName())) {
        override val left: TypeRef
            get() = interfaceType
        override val right: TypeRef
            get() = implementingType

        companion object {

            /**
             * Get all realizations from an annotation API TypeElement.
             */
            fun allOf(typeElement: TypeElement): List<Realization> = typeElement.interfaces
                    .filterIsInstance<DeclaredType>()
                    .mapNotNull { it.asTypeElement() }
                    .filter { it.getAnnotation(PlantUmlClass::class.java) != null }
                    .map { interfaceElement -> Realization(TypeRef.of(interfaceElement), TypeRef.of(typeElement)) }
        }
    }

    data class Inheritance(val superClassType: TypeRef, val implementingType: TypeRef) : BaseRelation(RelationId(superClassType.name.asQualifiedName())) {
        override val left: TypeRef
            get() = superClassType
        override val right: TypeRef
            get() = implementingType

        companion object {

            /**
             * Get the super class from an annotation API TypeElement.
             */
            fun of(typeElement: TypeElement): Inheritance? = typeElement.superclass
                .asTypeElement()?.let {
                    if (it.qualifiedName.toString() != "java.lang.Object" && it.getAnnotation(PlantUmlClass::class.java) != null)
                        Inheritance(TypeRef.of(it), TypeRef.of(typeElement))
                    else null
                }
        }
    }

    data class Association(
        val sourceType: TypeRef,
        val targetType: TypeRef,
        val field: FieldModel,
        val sourceCardinality: String = "",
        val targetCardinality: String = "",
        val type: AssociationType = STANDARD
    ) : BaseRelation(RelationId(field.name)) {

        override val left: TypeRef
            get() = sourceType
        override val right: TypeRef
            get() = targetType

        companion object {

            /**
             * Get all associations defined by the fields of an annotation API
             * TypeElement.
             */
            internal fun allOf(typeElement: TypeElement): List<Association> {
                val autoCreate = autoCreateAssociations(typeElement)

                return typeElement.enclosedElements
                    .filterIsInstance(VariableElement::class.java)
                    .filter { it.asType().asTypeElement()?.kind != ElementKind.ENUM || !it.asType().equals(typeElement.asType()) }
                    .filter { (autoCreate || fieldAnnotation(it) != null && fieldAnnotation(it)?.showAssociation == DEF_AUTO_ADD_ASSOCIATIONS) }
                    .flatMap { allOf(it, typeElement) }
            }

            private fun autoCreateAssociations(typeElement: TypeElement): Boolean {
                val autoCreateAssociations = typeElement.getAnnotation(PlantUmlClass::class.java)?.autoCreateAssociations ?: AutoCreateType.DEFAULT
                return (autoCreateAssociations == AutoCreateType.YES
                        || autoCreateAssociations == AutoCreateType.DEFAULT && configuration().getBoolean(KEY_AUTO_ADD_ASSOCIATIONS, DEF_AUTO_ADD_ASSOCIATIONS))
            }

            internal fun allOf(v: VariableElement, enclosingTypeElement: TypeElement) =
                if (targetsContainerType(v)) { containerTypeAssociations(enclosingTypeElement, v) }
                else standardTypeAssociation(enclosingTypeElement, v)

            private fun targetsContainerType(v: VariableElement) =
                (v.asType().asDeclaredType()?.typeArguments ?: emptyList()).isNotEmpty() && !(fieldAnnotation(v)?.forceStandardTypeAssociation ?: false)

            /**
             * Determines if the specified type is a container type (e.g. a
             * collection or optional type) and returns associations to all its
             * type parameters.
             */
            private fun containerTypeAssociations(sourceTypeElement: TypeElement, v: VariableElement): List<Association> =
                v.asType().asDeclaredType()
                    ?.let { targetTypes(it)?.map { t -> typeAssociation(sourceTypeElement, t, v, fieldAnnotation(v)) } }
                    ?: emptyList()

            /**
             * Get the target types of a field with a container type.
             */
            internal fun targetTypes(type: DeclaredType): Set<TypeRef>? = if (type.typeArguments.isNotEmpty()) {
                type.typeArguments.toSet()
                    .filter { it.asTypeElement()?.getAnnotation(PlantUmlClass::class.java) != null }
                    .filterIsInstance<DeclaredType>()
                    .map { TypeRef.of(it) }
                    .toSet()
            }
            else null

            /**
             * Returns a standard type association for a non-container type.
             */
            private fun standardTypeAssociation(sourceType: TypeElement, v: VariableElement) =
                if (v.asType().asTypeElement()?.getAnnotation(PlantUmlClass::class.java) != null)
                    listOfNotNull(typeAssociation(sourceType, TypeRef.of(v.asType()), v, fieldAnnotation(v)))
                else emptyList()

            private fun fieldAnnotation(it: VariableElement): PlantUmlField? = it.getAnnotation(PlantUmlField::class.java)

            private fun typeAssociation(sourceType: TypeElement, targetType: TypeRef, v: VariableElement, fieldAnnotation: PlantUmlField?) = Association(
                TypeRef.of(sourceType),
                targetType,
                FieldModel.of(v),
                fieldAnnotation?.sourceCardinality ?: "",
                fieldAnnotation?.targetCardinality ?: "",
                fieldAnnotation?.associationType ?: STANDARD
            )
        }
    }
}
