package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.S0tProcessor.Companion.configuration
import com.comsysto.livingdoc.s0t.annotation.plantuml.AutoCreateType
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField
import com.comsysto.livingdoc.s0t.asDeclaredType
import com.comsysto.livingdoc.s0t.model.AccessModifier.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

private const val KEY_AUTO_ADD_FIELDS = "s0t.plantuml.class.member.field.auto-add"
private const val DEF_AUTO_ADD_FIELDS = true

/**
 * Models a field.
 */
data class FieldModel(
    val name: String,
    val type: TypeRef,
    val accessModifier: AccessModifier,
    val typeParameters: List<TypeRef> = emptyList()
) {

    companion object {

        fun of(field: VariableElement) = FieldModel(
            field.simpleName.toString(),
            TypeRef.of(field.asType()),
            field.modifiers.map { asAccessModifier(it) }.find { it != null } ?: PACKAGE,
            field.asType().asDeclaredType()?.let { it.typeArguments.mapNotNull { arg -> TypeRef.of(arg) } } ?: emptyList())

        private fun asAccessModifier(modifier: Modifier): AccessModifier? = when (modifier) {
            Modifier.PUBLIC ->  PUBLIC
            Modifier.PROTECTED -> PROTECTED
            Modifier.DEFAULT -> PACKAGE
            Modifier.PRIVATE -> PRIVATE
            else -> null
        }

        /**
         * Get all the fields of a TypeElement.
         */
        fun allOf(typeElement: TypeElement): List<FieldModel> {
            val autoCreate = autoCreateFields(typeElement)

            return typeElement.enclosedElements
                .filterIsInstance(VariableElement::class.java)
                .filter { autoCreate || it.getAnnotation(PlantUmlField::class.java) != null }
                .map { of(it) }
        }

        private fun autoCreateFields(typeElement: TypeElement): Boolean {
            val autoCreateFields = typeElement.getAnnotation(PlantUmlClass::class.java)?.autoCreateFields ?: AutoCreateType.DEFAULT
            return (autoCreateFields == AutoCreateType.YES
                    || autoCreateFields == AutoCreateType.DEFAULT && configuration().getBoolean(KEY_AUTO_ADD_FIELDS, DEF_AUTO_ADD_FIELDS))
        }
    }
}