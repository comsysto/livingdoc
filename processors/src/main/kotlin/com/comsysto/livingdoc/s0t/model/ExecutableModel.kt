package com.comsysto.livingdoc.s0t.model

import com.comsysto.livingdoc.s0t.S0tProcessor
import com.comsysto.livingdoc.s0t.S0tProcessor.Companion.environment
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable
import com.comsysto.livingdoc.s0t.model.TypeName.ComplexTypeName
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.UnsolvedSymbolException
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.lang.model.element.ExecutableElement
import javax.tools.StandardLocation

/**
 * Models a sequence of executable members in a tree-like structure.
 */
data class ExecutableModel(

    /**
     * The name of the executable.
     */
    val name: ExecutableName,

    /**
     * The outgoing calls from this executable's body.
     */
    val outgoingCalls: List<ExecutableModel>,

    /**
     * The executables signature including parameters.
     */
    val signature: String? = null,
) {

    companion object {
        private val log = LoggerFactory.getLogger(ExecutableModel::class.java.name)

        /**
         * Creates an instance from an ExecutableElement. This is usually only
         * used for the start of a sequence (annotated with @StartOfSequence),
         * as all other models are created from _JavaParser_ output.
         */
        fun of(element: ExecutableElement): ExecutableModel? {
            val enclosingType = TypeRef.of(element.enclosingElement.asType())
            val compilationUnit = compilationUnit(enclosingType.name.packageName, enclosingType.name.simpleName)

            val name = ExecutableName(enclosingType.name as ComplexTypeName, element.simpleName.toString())
            return compilationUnit?.let { ExecutableModel(name, outgoingCalls(it, name)) }
        }

        /**
         * Creates an instance from a ResolvedMethodDeclaration returned by
         * _JavaParser_. This method will be called recursively to create the
         * whole sequence, resulting in a tree of executable elements.
         */
        fun of(resolved: ResolvedMethodDeclaration): ExecutableModel? {
            val compilationUnit = compilationUnit(resolved.packageName, resolved.className)
            val name = ExecutableName(ComplexTypeName(resolved.packageName, resolved.className), resolved.name)

            return compilationUnit?.let { ExecutableModel(name, outgoingCalls(it, name), resolved.signature) }
        }

        /**
         * Parse a Java class with the specified package and type named using
         * _JavaParser_.
         *
         * @return the compilation unit
         */
        private fun compilationUnit(packageName: String, simpleTypeName: String): CompilationUnit? =
            environment().sourcePath(packageName, simpleTypeName)?.let {
                val typeSolver = CombinedTypeSolver(JavaParserTypeSolver(environment().root), ReflectionTypeSolver())

                StaticJavaParser.getConfiguration().setSymbolResolver(JavaSymbolSolver(typeSolver))
                return StaticJavaParser.parse(it.toFile())
            }

        /**
         * Get the ExecutableModel for an executable's outgoing calls from the
         * compilation unit.
         */
        private fun outgoingCalls(compilationUnit: CompilationUnit, executableName: ExecutableName): List<ExecutableModel> {
            return methodDeclarations(compilationUnit, executableName)
                .map { it.body.orElse(null) }
                .flatMap { block -> block.findAll(MethodCallExpr::class.java).mapNotNull { resolveMethod(it) } }
                .mapNotNull { of(it) }
        }

        private fun resolveMethod(it: MethodCallExpr) = try {
            it.resolve()
        } catch (e: UnsolvedSymbolException) {
            log.warn(e.message, e)
            null
        }

        private fun methodDeclarations(unit: CompilationUnit, executableName: ExecutableName): List<MethodDeclaration> {
            return unit.types
                .filter { type -> type.fullyQualifiedName == Optional.of(executableName.typeName.asQualifiedName()) }
                .flatMap { type -> type.methods }
                .filter { method -> method.getAnnotationByClass(PlantUmlExecutable::class.java).isPresent && method.nameAsString == executableName.name }
        }
    }
}