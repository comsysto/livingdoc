package model

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable
import com.comsysto.livingdoc.kotlin.annotation.processors.model.DiagramId
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver
import java.io.File
import java.util.*
import java.util.Collections.emptyList
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

class ExecutablePart(
        private val env: ProcessingEnvironment,
        internal val diagramIds: Set<DiagramId>,
        internal val annotation: PlantUmlExecutable,
        internal val annotated: ExecutableElement) {
    internal var outgoingDeclarations: List<ResolvedMethodDeclaration>

    init {
        outgoingDeclarations = parse(annotated, env)
    }

    companion object {

        fun parse(
                annotated: ExecutableElement,
                env: ProcessingEnvironment): List<ResolvedMethodDeclaration> {
            val declaringType = getDeclaringType(annotated)
            val unit = getCompilationUnit(
                    env.elementUtils
                            .getPackageOf(annotated).toString(),
                    declaringType.simpleName.toString())

            return getOutgoingCalls(
                    unit,
                    declaringType.qualifiedName.toString(),
                    annotated.simpleName.toString())
        }

        internal fun getCompilationUnit(packageName: String, simpleTypeName: String): CompilationUnit {
            val root = "/home/fsc/work/projects/labs/livingdoc/annotation-processors-kotlin/src/test/kotlin"
            val path = File(root, packageName.replace("\\.".toRegex(), File.separator))

            val typeSolver = CombinedTypeSolver(
                    JavaParserTypeSolver(root),
                    ReflectionTypeSolver())
            val symbolSolver = JavaSymbolSolver(typeSolver)
            StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver)

            val file = File(path, "$simpleTypeName.java")
            return StaticJavaParser.parse(file)
        }

        fun getDeclaringType(annotated: ExecutableElement): TypeElement {
            return annotated.enclosingElement as TypeElement
        }

        internal fun getOutgoingCalls(
                unit: CompilationUnit,
                declaringTypeName: String,
                methodName: String): List<ResolvedMethodDeclaration> {
            return getMethodDeclaration(
                    unit,
                    declaringTypeName,
                    methodName)
                    .flatMap { it.body }
                    .map<List<ResolvedMethodDeclaration>> { block ->
                        block.findAll(MethodCallExpr::class.java)
                                .map { it.resolve() }
                                .toList()
                    }
                    .orElse(emptyList())
        }

        private fun getMethodDeclaration(
                unit: CompilationUnit,
                declaringTypeName: String,
                methodName: String): Optional<MethodDeclaration> {
            return unit.types.stream()
                    .filter { type -> type.fullyQualifiedName == Optional.of(declaringTypeName) }
                    .flatMap { type -> type.methods.stream() }
                    .filter { method -> method.getAnnotationByClass(PlantUmlExecutable::class.java).isPresent && method.nameAsString == methodName }
                    .findFirst()
        }
    }
}
