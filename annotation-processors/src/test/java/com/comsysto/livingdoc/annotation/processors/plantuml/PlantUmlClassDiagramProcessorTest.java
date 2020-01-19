package com.comsysto.livingdoc.annotation.processors.plantuml;

import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.KEY_OUT_DIR;
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

    protected static final String TEST_DIR = "pumlgen.test.dir";
    private File exampleDir;

    @Before
    public void setUp() {
        final File testRoot = Optional.ofNullable(System.getProperty(TEST_DIR))
            .map(File::new)
            .orElseGet(this::getTestClassLoaderRoot);
        exampleDir = new File(testRoot, "com/comsysto/livingdoc/example");
    }

    @SneakyThrows
    private File getTestClassLoaderRoot() {
        final File projectRoot = Paths.get(this.getClass().getClassLoader().getResource(".").toURI())
            .getParent()
            .getParent()
            .getParent()
            .toFile();
        return new File(projectRoot, "src/test/java");
    }

    @Test
    @SneakyThrows
    public void should_run_successfully_and_produce_expected_output() {
        final Compilation result = Compiler.javac()
            .withProcessors(new PlantUmlClassDiagramProcessor())
            .withOptions("-Apumlgen.out.dir=out")
            .compile(stream(exampleDir.listFiles())
                         .map(this::javaFileObject)
                         .collect(toList()));

        assertThat(result.status()).isEqualTo(Status.SUCCESS);

        // Check that the file exists and contains a selected sample of expected
        // lines:
        assertThat(new File(System.getProperty(KEY_OUT_DIR, "out"), "package_class.puml"))
            .exists()
            .satisfies(file -> assertThat(lines(file))
                .contains(
                    "class Car",
                    "Wing leftWing",
                    "Flying <|.. FlyingVehicle",
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