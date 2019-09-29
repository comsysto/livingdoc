package com.comsysto.livingdoc.kotlin.annotation.processors.model

import org.apache.commons.lang3.StringUtils
import java.util.*
import javax.lang.model.element.Name
import javax.lang.model.type.TypeMirror

/**
 * Models a class diagram. The methods of this class are manly used by the
 * corresponding freemarker template (`class-diagram.puml.ftl`).
 */
class ClassDiagram(

        /**
         * the diagram title.
         */
        title: String? = null,

        /**
         * Any PlantUml files to be included.
         */
        includeFiles: List<String> = listOf(),

        /**
         * The type parts to be rendered in this diagram.
         */
        val parts: List<TypePart> = listOf()

) : Diagram(title, includeFiles) {

    /**
     * Get all inheritance or realization relations within this diagram.
     *
     * @return the inheritance relations.
     */
    val inheritanceRelations: List<RelationPart>
        get() {
            val whitelist = renderedTypeNames()

            return parts
                    .flatMap { it.relations }
                    .filter { EnumSet.of(Relation.INHERITANCE, Relation.REALIZATION).contains(it.relation) }
                    .filter { shouldBeRendered(it, whitelist) }
        }

    /**
     * Get all (field) associations within this diagram.
     *
     * @return the list of association relation parts.
     */
    val associations: List<RelationPart>
        get() {
            val whitelist = renderedTypeNames()

            return parts
                    .flatMap { it.relations }
                    .filter { it.relation == Relation.ASSOCIATION }
                    .filter { shouldBeRendered(it, whitelist) }
        }

    /**
     * Get the names of all types to be rendered within this diagram.
     *
     * @return the list of type names.
     */
    private fun renderedTypeNames(): Set<Name> {
        return parts
                .map { it.typeElement.simpleName }
                .toSet()
    }

    /**
     * Determine if a relation should be rendered in this diagram.
     *
     * @param relationPart the relation to be checked.
     * @param whitelist    the list of type names to be rendered in this diagram.
     *
     * @return true if the type should be rendered.
     */
    private fun shouldBeRendered(relationPart: RelationPart, whitelist: Set<Name>): Boolean {
        return whitelist.any { name -> name.contentEquals(relationPart.right.simpleName) }
                && whitelist.any { name -> name.contentEquals(relationPart.left.simpleName) }
    }

    companion object {

        /**
         * Get the simple name of a type.
         *
         * @param typeMirror the type mirror.
         *
         * @return the simple name.
         */
        fun simpleTypeName(typeMirror: TypeMirror): String {
            return if (typeMirror.toString().contains("."))
                StringUtils.substringAfterLast(typeMirror.toString(), ".")
            else
                typeMirror.toString()
        }
    }
}
