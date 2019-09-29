package com.comsysto.livingdoc.kotlin.annotation.processors.render.plantuml

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote
import com.comsysto.livingdoc.kotlin.annotation.processors.model.*
import com.comsysto.livingdoc.kotlin.annotation.processors.model.ClassDiagram.Companion.simpleTypeName
import com.comsysto.livingdoc.kotlin.annotation.processors.model.Relation.*
import com.github.javaparser.ast.expr.NormalAnnotationExpr
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import model.ExecutablePart
import model.ExecutablePart.Companion.getCompilationUnit
import model.ExecutablePart.Companion.getDeclaringType
import model.ExecutablePart.Companion.getOutgoingCalls
import java.util.*
import java.util.stream.Collectors
import javax.lang.model.element.VariableElement

object PlantUmlDiagramRenderer {

    fun renderDiagram(diagram: Diagram) =
        """
        |@startuml${if (diagram.includeFiles.isNotEmpty()) renderIncludeFiles(diagram) else ""}
        |
        |hide empty members
        |${diagram.title.let { "title $it" }}
        |${renderDiagramParts(diagram)}
        |@enduml
        """.trimMargin()

    private fun renderDiagramParts(diagram: Diagram) = when (diagram) {
        is ClassDiagram -> renderClassDiagramParts(diagram)
        is SequenceDiagram -> renderSequenceDiagramParts(diagram)
        else -> ""
    }

    private fun renderClassDiagramParts(diagram: ClassDiagram) =
            """
        |${diagram.parts.joinToString("\n") { renderType(it) }}
        |
        |${diagram.inheritanceRelations.joinToString("\n") { renderRelation(it) }}
        |
        |${diagram.associations.joinToString("\n") { renderRelation(it) }}
        """.trimMargin()

    private fun renderSequenceDiagramParts(diagram: SequenceDiagram) =
            """
        |${diagram.parts.joinToString("\n") { "participant ${simpleTypeName(it.annotated.enclosingElement.asType())}" }}
        |
        |${diagram.parts.joinToString("\n") { renderExecutable(it) }}
        """.trimMargin()


    private fun renderIncludeFiles(diagram: Diagram) =
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

    private fun renderField(it: VariableElement) = "${simpleTypeName(it.asType())} ${it.simpleName}"

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

    private fun renderExecutable(part: ExecutablePart): String {
        return render(
                getDeclaringType(part.annotated).simpleName.toString(),
                part.outgoingDeclarations,
                Optional.empty())
    }

    private fun render(
            className: String,
            outgoingCalls: List<ResolvedMethodDeclaration>,
            renderedNote: Optional<String>) =
            "activate $className\n${renderedNote.orElse("")}${renderCalls(className, outgoingCalls)}deactivate $className\n"

    private fun renderCalls(className: String, outgoingCalls: List<ResolvedMethodDeclaration>) =
            if (outgoingCalls.isEmpty())
                ""
            else
                outgoingCalls.joinToString("\n") {
                    String.format(
                            "%s -> %s: %s\n%s",
                            className,
                            it.className,
                            it.signature,
                            render(it))
                }

    private fun render(method: ResolvedMethodDeclaration): String {
        val className = method.className
        val outgoingCalls = getOutgoingCalls(
                getCompilationUnit(method.packageName, className),
                String.format("%s.%s", method.packageName, className),
                method.name)
        return render(className, outgoingCalls, method.toAst()
                .flatMap { astMethod -> astMethod.getAnnotationByClass(PlantUmlNote::class.java) }
                .filter { NormalAnnotationExpr::class.java.isInstance(it) }
                .map { NormalAnnotationExpr::class.java.cast(it) }
                .map { annotation ->
                    val members = annotation.pairs.stream().collect(Collectors.toList())
                            .map { it.getNameAsString() to it.getValue() }
                            .toMap()

                    String.format(
                            "note %s\n%s\nend note\n",
                            members["position"]?.asNameExpr()?.name,
                            members["body"]?.asStringLiteralExpr()?.asString())
                })
    }
}