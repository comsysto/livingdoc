package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.*
import com.comsysto.livingdoc.s0t.S0tProcessor.Companion.configuration
import com.comsysto.livingdoc.s0t.annotation.plantuml.AutoCreateType
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType.STANDARD
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

    /**
     * The 'left' side of the relation. In case if Realization and Inheritance,
     * this is the parent type. In case of an Association, this is the type
     * owning the respective field declaration.
     */
    val left: TypeRef

    /**
     * The 'right' side of the relation. In case if Realization and Inheritance,
     * this is the child type. In case of an Association, this is the referenced
     * type.
     */
    val right: TypeRef

    abstract class BaseRelation(val id: RelationId) : Relation

    /**
     * A Realization models an interface being implemented by a concrete type.
     *
     * @param interfaceType the implemented interface type.
     * @param implementingType the concrete type implementing the interface.
     */
    data class Realization(val interfaceType: TypeRef, val implementingType: TypeRef) : BaseRelation(RelationId(interfaceType.name.asQualifiedName())) {
        override val left: TypeRef
            get() = interfaceType
        override val right: TypeRef
            get() = implementingType

        companion object {

            /**
             * Get all interfaces implemented by a type element.
             */
            fun allOf(typeElement: TypeElement): List<Realization> = typeElement.interfaces
                .filterIsInstance<DeclaredType>()
                .mapNotNull { it.asTypeElement() }
                .filter { isPartOfDiagram(it) }
                .map { interfaceElement -> Realization(interfaceType = TypeRef.of(interfaceElement), implementingType = TypeRef.of(typeElement)) }
        }
    }

    data class Inheritance(val superClassType: TypeRef, val implementingType: TypeRef) : BaseRelation(RelationId(superClassType.name.asQualifiedName())) {
        override val left: TypeRef
            get() = superClassType
        override val right: TypeRef
            get() = implementingType

        companion object {

            /**
             * Get the super class of a type element if it is present on the
             * diagram.
             */
            fun of(typeElement: TypeElement): Inheritance? = typeElement.superclass
                .asTypeElement()?.let {
                    if (isPartOfDiagram(it))
                        Inheritance(superClassType = TypeRef.of(it), implementingType = TypeRef.of(typeElement))
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
                return typeElement.enclosedElements
                    .filterIsInstance(VariableElement::class.java)
                    .filter { !isSelfReference(it, typeElement) }
                    .filter { (fieldAnnotation(it)?.showAssociation == true || shouldAutoCreateAssociations(typeElement)) }
                    .flatMap { allOf(it, typeElement) }
            }

            private fun shouldAutoCreateAssociations(typeElement: TypeElement): Boolean {
                val autoCreateAssociations = typeElement.getAnnotation(PlantUmlClass::class.java)?.autoCreateAssociations ?: AutoCreateType.DEFAULT
                return (autoCreateAssociations == AutoCreateType.YES
                        || autoCreateAssociations == AutoCreateType.DEFAULT && configuration().getBoolean(KEY_AUTO_ADD_ASSOCIATIONS, DEF_AUTO_ADD_ASSOCIATIONS))
            }

            private fun isSelfReference(it: VariableElement, typeElement: TypeElement) = it.asType().equals(typeElement.asType())

            internal fun allOf(v: VariableElement, enclosingTypeElement: TypeElement) =
                if (targetsContainerType(v)) {
                    containerTypeAssociations(enclosingTypeElement, v)
                } else standardTypeAssociation(enclosingTypeElement, v)

            private fun targetsContainerType(v: VariableElement) = v.asType().typeArguments().isNotEmpty() && !forceStandardTypeAssociation(v)

            private fun forceStandardTypeAssociation(v: VariableElement) = fieldAnnotation(v)?.forceStandardTypeAssociation ?: false

            /**
             * Determines if the specified type is a container type (e.g. a
             * collection or optional type) and returns associations to all its
             * type parameters.
             */
            private fun containerTypeAssociations(enclosingTypeElement: TypeElement, v: VariableElement): List<Association> =
                v.asType().asDeclaredType()
                    ?.let { targetTypes(it)?.map { t -> typeAssociation(from = enclosingTypeElement, to = t, v) } }
                    ?: emptyList()

            /**
             * Get the target types of a field with a container type.
             */
            internal fun targetTypes(type: DeclaredType): Set<TypeRef>? = if (type.typeArguments.isNotEmpty()) {
                type.typeArguments.toSet()
                    .filter { isPartOfDiagram(it) }
                    .filterIsInstance<DeclaredType>()
                    .map { TypeRef.of(it) }
                    .toSet()
            } else null

            /**
             * Returns a standard type association (for a non-container field type).
             */
            private fun standardTypeAssociation(enclosingType: TypeElement, v: VariableElement) =
                if (isPartOfDiagram(v.asType())) listOfNotNull(typeAssociation(from = enclosingType, to = TypeRef.of(v.asType()), v))
                else emptyList()

            private fun typeAssociation(from: TypeElement, to: TypeRef, v: VariableElement): Association {
                val a = fieldAnnotation(v)

                return Association(
                    TypeRef.of(from),
                    to,
                    FieldModel.of(v),
                    a?.sourceCardinality ?: "",
                    a?.targetCardinality ?: "",
                    a?.associationType ?: STANDARD
                )
            }

            private fun fieldAnnotation(v: VariableElement): PlantUmlField? = v.getAnnotation(PlantUmlField::class.java)
        }
    }
}
