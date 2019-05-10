package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compilation.Status;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import javax.tools.JavaFileObject;

@SuppressWarnings("ConstantConditions")
public class PlantUmlClassDiagramProcessorTest {

    private File exampleDir;

    @Before
    public void setUp() {
        final File projectRoot = Optional.ofNullable(System.getProperty("project.test.dir"))
            .map(File::new)
            .orElseGet(this::getTestClassLoaderRoot);
        exampleDir = new File(projectRoot, "src/test/java/com/comsysto/livingdoc/example");
    }

    @SneakyThrows
    private File getTestClassLoaderRoot() {
        return Paths.get(this.getClass().getClassLoader().getResource(".").toURI())
            .getParent()
            .getParent()
            .getParent()
            .toFile();
    }

    @Test
    @SneakyThrows
    public void should_run_successfully_and_produce_expected_output() {
        final Compilation result = Compiler.javac()
            .withProcessors(new PlantUmlClassDiagramProcessor())
            .compile(stream(exampleDir.listFiles())
                         .map(this::javaFileObject)
                         .collect(toList()));

        assertThat(result.status()).isEqualTo(Status.SUCCESS);

        // Check that the file exists and contains a selected sample of expected
        // lines:
        assertThat(new File("package_class.puml"))
            .exists()
            .satisfies(file -> assertThat(lines(file))
                .contains(
                    "class Car",
                    "Wing leftWing",
                    "FlyingVehicle <|-- Airplane",
                    "Airplane --> Wing"));
    }

    @SneakyThrows
    private Set<String> lines(final File file) {
        return Files.lines(file.toPath())
            .map(String::trim)
            .collect(toSet());
    }

    @SneakyThrows
    private JavaFileObject javaFileObject(final File f) {
        return JavaFileObjects.forResource(f.toURI().toURL());
    }
}