package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lombok.SneakyThrows;
import lombok.Value;

import java.io.File;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

@Value
public class ExecutablePart {
    ProcessingEnvironment env;
    Set<DiagramId> diagramIds;
    PlantUmlExecutable annotation;
    ExecutableElement annotated;

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
    }

    @SneakyThrows
    public List<String> parse() {
        final PackageElement pkg = env.getElementUtils().getPackageOf(annotated);
        final String root = "/home/fsc/work/projects/labs/livingdoc/annotation-processors/src/test/java";
        final File path = new File(root, pkg.toString().replaceAll("\\.", File.separator));

        final TypeSolver typeSolver = new CombinedTypeSolver(
            new JavaParserTypeSolver(root),
            new ReflectionTypeSolver());
        final JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);


        final File file = new File(path, getDeclaringType().getSimpleName() + ".java");
        final CompilationUnit unit = StaticJavaParser.parse(file);

        return getMethodDeclaration(unit, file).getBody()
            .map(block -> block.findAll(MethodCallExpr.class).stream()
                .map(MethodCallExpr::resolve)
                .map(expr -> String.format(
                    "%s -> %s: %s",
                    getDeclaringType().getSimpleName(),
                    expr.getClassName(),
                    expr.getSignature()))
                .collect(toList()))
            .orElse(emptyList());
    }

    private MethodDeclaration getMethodDeclaration(final CompilationUnit unit, final File file) {
        return unit.getTypes().stream()
            .filter(type -> type.getFullyQualifiedName()
                .equals(Optional.of(getDeclaringType().getQualifiedName().toString())))
            .flatMap(type -> type.getMethods().stream())
            .filter(method -> method.getAnnotationByClass(PlantUmlExecutable.class).isPresent())
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException(String.format(
                "Could not find executable %s in compilation unit %s and type %s.",
                annotated.getSimpleName(),
                file,
                getDeclaringType().getQualifiedName().toString())));
    }

    public TypeElement getDeclaringType() {
        return (TypeElement) annotated.getEnclosingElement();
    }
}
