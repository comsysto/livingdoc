package com.comsysto.livingdoc.s0t.model.relations

import com.comsysto.livingdoc.s0t.S0tProcessor
import com.comsysto.livingdoc.s0t.annotation.plantuml.AutoCreateType
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField
import com.comsysto.livingdoc.s0t.apextensions.asDeclaredType
import com.comsysto.livingdoc.s0t.apextensions.isPlantUmlClass
import com.comsysto.livingdoc.s0t.model.FieldModel
import com.comsysto.livingdoc.s0t.model.TypeRef
import com.comsysto.livingdoc.s0t.apextensions.typeArguments
import com.comsysto.livingdoc.s0t.model.TypeModel
import com.comsysto.livingdoc.s0t.model.TypeName
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType

data class Association(
    val sourceType: TypeRef,
    val targetType: TypeRef,
    val field: FieldModel,
    val sourceCardinality: String = "",
    val targetCardinality: String = "",
    val type: PlantUmlField.AssociationType = PlantUmlField.AssociationType.STANDARD
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
                    || autoCreateAssociations == AutoCreateType.DEFAULT && S0tProcessor.configuration()
                .getBoolean(KEY_AUTO_ADD_ASSOCIATIONS, DEF_AUTO_ADD_ASSOCIATIONS))
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
                .filter { isPlantUmlClass(it) }
                .filterIsInstance<DeclaredType>()
                .map { TypeRef.of(it) }
                .toSet()
        } else null

        /**
         * Returns a standard type association (for a non-container field type).
         */
        private fun standardTypeAssociation(enclosingType: TypeElement, v: VariableElement) =
            if (isPlantUmlClass(v.asType())) listOfNotNull(typeAssociation(from = enclosingType, to = TypeRef.of(v.asType()), v))
            else emptyList()

        private fun typeAssociation(from: TypeElement, to: TypeRef, v: VariableElement): Association {
            val a = fieldAnnotation(v)

            return Association(
                TypeRef.of(from),
                to,
                FieldModel.of(v),
                a?.sourceCardinality ?: "",
                a?.targetCardinality ?: "",
                a?.associationType ?: PlantUmlField.AssociationType.STANDARD
            )
        }

        private fun fieldAnnotation(v: VariableElement): PlantUmlField? = v.getAnnotation(PlantUmlField::class.java)
    }
}