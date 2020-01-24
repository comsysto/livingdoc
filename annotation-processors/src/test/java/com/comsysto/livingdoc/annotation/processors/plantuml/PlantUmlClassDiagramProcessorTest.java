package com.comsysto.livingdoc.annotation.processors.plantuml;

import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.DEF_OUT_DIR;
import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.KEY_OUT_DIR;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compilation.Status;
import com.google.testing.compile.Compiler;
import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileSystemNotFoundException;
import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class PlantUmlClassDiagramProcessorTest {

    private File exampleDir;

    @Before
    public void setUp() {
        final File testRoot = Optional.ofNullable(System.getProperty(PlantUmlDiagramTestUtils.TEST_DIR))
            .map(File::new)
            .orElseGet(() -> PlantUmlDiagramTestUtils.getTestClassLoaderRoot(this.getClass().getClassLoader()));
        exampleDir = new File(testRoot, "example");
    }

    @Test
    @SneakyThrows
    public void should_run_successfully_and_produce_expected_output() {
        final Compilation result = Compiler.javac()
            .withProcessors(new PlantUmlClassDiagramProcessor())
            .withOptions("-Apumlgen.out.dir=out")
            .compile(Optional.ofNullable(exampleDir.listFiles())
                         .map(Arrays::stream)
                         .orElseThrow(() -> new FileSystemNotFoundException(exampleDir.getAbsolutePath()))
                         .map(PlantUmlDiagramTestUtils::javaFileObject)
                         .collect(toList()));

        assertThat(result.status()).isEqualTo(Status.SUCCESS);

        // Check that the file exists and contains a selected sample of expected
        // lines:
        assertThat(new File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "package_class.puml"))
            .exists()
            .satisfies(file -> assertThat(PlantUmlDiagramTestUtils.lines(file))
                .contains(
                    "class Car",
                    "Wing leftWing",
                    "Flying <|.. FlyingVehicle",
                    "FlyingVehicle <|-- Airplane",
                    "Airplane --> Wing"));
    }

}