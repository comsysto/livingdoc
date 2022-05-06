package com.comsysto.livingdoc.s0t.model.relations

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlDependency
import com.comsysto.livingdoc.s0t.model.TypeRef
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

/**
 * Models a custom dependency between two types.
 */
data class Dependency(
    val sourceType: TypeRef,
    val targetType: TypeRef,
    val description: String
) : BaseRelation(RelationId(targetType.name.asQualifiedName())) {

    override val left: TypeRef
        get() = sourceType
    override val right: TypeRef
        get() = targetType

    companion object {
        internal fun allOf(typeElement: TypeElement): List<Dependency> {
            return typeElement.getAnnotationsByType(PlantUmlDependency::class.java)
                .map { Dependency(TypeRef.of(typeElement), TypeRef.of(typeMirror(it)), it.description) }
        }

        /**
         * Uses the simplification described in
         * <a href=https://area-51.blog/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor>
         *     Getting Class values from Annotations in an AnnotationProcessor</a> to retrieve the type mirror for an
         *     annotation element with return type ``Class``.
         */
        private fun typeMirror(it: PlantUmlDependency) = try {
            it.target
            throw NotImplementedError("Unreachable code")
        } catch (e: MirroredTypeException) {
            e.typeMirror
        }
    }
}
