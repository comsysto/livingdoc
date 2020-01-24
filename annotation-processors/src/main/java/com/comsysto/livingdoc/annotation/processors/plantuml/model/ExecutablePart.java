package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@Value
public class ExecutablePart {

    ProcessingEnvironment env;
    Set<DiagramId> diagramIds;
    PlantUmlExecutable annotation;
    ExecutableElement annotated;
    List<ResolvedMethodDeclaration> outgoingDeclarations;

    public ExecutablePart(
        final ProcessingEnvironment env,
        final Set<DiagramId> diagramIds,
        final PlantUmlExecutable annotation,
        final ExecutableElement annotated)
    {
        this.env = env;
        this.diagramIds = diagramIds;
        this.annotation = annotation;
        this.annotated = annotated;

        outgoingDeclarations = parse(annotated, env);
    }

    @SneakyThrows
    public static List<ResolvedMethodDeclaration> parse(
        final ExecutableElement annotated,
        final ProcessingEnvironment env)
    {
        final TypeElement declaringType = getDeclaringType(annotated);
        final CompilationUnit unit = getCompilationUnit(
            env.getElementUtils()
                .getPackageOf(annotated).toString(),
            declaringType.getSimpleName().toString());

        return getOutgoingCalls(
            unit,
            declaringType.getQualifiedName().toString(),
            annotated.getSimpleName().toString());
    }

    @SneakyThrows
    private static CompilationUnit getCompilationUnit(final String packageName, final String simpleTypeName) {
        final String root = "/home/fsc/work/projects/labs/livingdoc/annotation-processors/src/test/java";
        final File path = new File(root, packageName.replaceAll("\\.", File.separator));

        final TypeSolver typeSolver = new CombinedTypeSolver(
            new JavaParserTypeSolver(root),
            new ReflectionTypeSolver());
        final JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        final File file = new File(path, simpleTypeName + ".java");
        return StaticJavaParser.parse(file);
    }

    public static TypeElement getDeclaringType(final ExecutableElement annotated) {
        return (TypeElement) annotated.getEnclosingElement();
    }

    private static List<ResolvedMethodDeclaration> getOutgoingCalls(
        final CompilationUnit unit,
        final String declaringTypeName,
        final String methodName)
    {
        return getMethodDeclaration(
            unit,
            declaringTypeName,
            methodName)
            .flatMap(MethodDeclaration::getBody)
            .map(block -> block.findAll(MethodCallExpr.class).stream()
                .map(MethodCallExpr::resolve)
                .collect(toList()))
            .orElse(emptyList());
    }

    private static Optional<MethodDeclaration> getMethodDeclaration(
        final CompilationUnit unit,
        final String declaringTypeName,
        final String methodName)
    {
        return unit.getTypes().stream()
            .filter(type -> type.getFullyQualifiedName()
                .equals(Optional.of(declaringTypeName)))
            .flatMap(type -> type.getMethods().stream())
            .filter(method -> method.getAnnotationByClass(PlantUmlExecutable.class).isPresent()
                              && method.getNameAsString()
                                  .equals(methodName))
            .findFirst();
    }

    public String render() {
        return render(
            getDeclaringType(annotated).getSimpleName().toString(),
            getOutgoingDeclarations(),
            Optional.empty());
    }

    private String render(
        final String className,
        final List<ResolvedMethodDeclaration> outgoingCalls,
        final Optional<String> renderedNote)
    {
        if (!outgoingCalls.isEmpty() || renderedNote.isPresent()) {
            return String.format(
                "activate %s\n%s%sdeactivate %s\n",
                className,
                renderedNote.orElse(""),
                renderCalls(className, outgoingCalls),
                className);
        } else {
            return "\n";
        }
    }

    private String renderCalls(final String className, final List<ResolvedMethodDeclaration> outgoingCalls) {
        return outgoingCalls.isEmpty()
               ? ""
               : outgoingCalls.stream()
                   .map(call -> String.format(
                       "%s -> %s: %s\n%s",
                       className,
                       call.getClassName(),
                       call.getSignature(),
                       render(call)))
                   .collect(joining("\n"));
    }

    private String render(final ResolvedMethodDeclaration method) {
        final String className = method.getClassName();
        final List<ResolvedMethodDeclaration> outgoingCalls = getOutgoingCalls(
            getCompilationUnit(method.getPackageName(), className),
            String.format("%s.%s", method.getPackageName(), className),
            method.getName());
        return render(className, outgoingCalls, method.toAst()
            .flatMap(astMethod -> astMethod.getAnnotationByClass(PlantUmlNote.class))
            .filter(NormalAnnotationExpr.class::isInstance)
            .map(NormalAnnotationExpr.class::cast)
            .map(annotation -> {
                final Map<String, Expression> members = annotation.getPairs().stream()
                    .collect(toMap(MemberValuePair::getNameAsString, MemberValuePair::getValue));
                return String.format(
                    "note %s\n%s\nend note\n",
                    members.get("position").asNameExpr().getName(),
                    members.get("body").asStringLiteralExpr().asString());
            }));
    }
}
