package com.comsysto.livingdoc.kotlin.annotation.processors.render.plantuml

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.kotlin.annotation.processors.model.ClassDiagram
import com.comsysto.livingdoc.kotlin.annotation.processors.model.Relation
import com.comsysto.livingdoc.kotlin.annotation.processors.model.Relation.*
import com.comsysto.livingdoc.kotlin.annotation.processors.model.RelationPart
import com.comsysto.livingdoc.kotlin.annotation.processors.model.TypePart
import javax.lang.model.element.VariableElement

object PlantUmlClassDiagramRenderer {

    fun renderDiagram(diagram: ClassDiagram) =
        """
        |@startuml${if (diagram.includeFiles.isNotEmpty()) renderIncludeFiles(diagram) else ""}
        |
        |hide empty members
        |${diagram.title.let { "title $it" }}
        |${diagram.parts.joinToString("\n") { renderType(it) }}
        |
        |${diagram.inheritanceRelations.joinToString("\n") { renderRelation(it) }}
        |
        |${diagram.associations.joinToString("\n") { renderRelation(it) }}
        |@enduml
        """.trimMargin()

    private fun renderIncludeFiles(diagram: ClassDiagram) =
        """
        ${diagram.includeFiles.joinToString { "|\n!include $it" }}""".trimMargin()

    private fun renderType(part: TypePart) =
            """
        |${renderTypeDeclaration(part)} ${part.name} {
        |    ${part.annotatedFields.joinToString("\n|    ") { renderField(it) }}
        |}
        |${part.notes.joinToString("\n|") { renderNote(part, it) }}
        |
        """.trimMargin()

    private fun renderTypeDeclaration(part: TypePart) = when {
        part.isInterface -> "interface"
        part.isAbstract -> "abstract class"
        part.isEnum -> "enum"
        else -> "class"
    }

    private fun renderField(it: VariableElement) = "${ClassDiagram.simpleTypeName(it.asType())} ${it.simpleName}"

    private fun renderRelation(part: RelationPart) = "${part.left.simpleName} ${renderRelation(part.relation)} ${part.right.simpleName}"

    private fun renderRelation(relation: Relation) = when (relation) {
        REALIZATION -> "<|.."
        INHERITANCE -> "<|--"
        ASSOCIATION -> "-->"
    }

    private fun renderNote(part: TypePart, note: PlantUmlNote) =
            """
            |note ${note.position.name.toLowerCase()} of ${part.name}
            |${note.body.lines().joinToString("\n")}
            |end note
            """.trimMargin()
}