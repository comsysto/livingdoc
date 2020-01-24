package com.comsysto.livingdoc.annotation.processors.plantuml;

import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.DEF_OUT_DIR;
import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.KEY_OUT_DIR;
import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlDiagramTestUtils.getTestClassLoaderRoot;
import static com.google.testing.compile.Compilation.Status.SUCCESS;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Optional;

public class PlantUmlSequenceDiagramProcessorTest {
    protected static final String TEST_DIR = "pumlgen.test.dir";
    private File exampleDir;

    @Before
    public void setup() {
        final File testRoot = Optional.ofNullable(System.getProperty(TEST_DIR))
            .map(File::new)
            .orElseGet(() -> getTestClassLoaderRoot(this.getClass().getClassLoader()));
        exampleDir = new File(testRoot, "example");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void should_run_successfully_and_produce_expected_output() {
        final Compilation result = Compiler.javac()
            .withProcessors(new PlantUmlClassDiagramProcessor())
            .withOptions("-Apumlgen.out.dir=out")
            .compile(stream(exampleDir.listFiles())
                         .map(PlantUmlDiagramTestUtils::javaFileObject)
                         .collect(toList()));

        assertThat(result.status()).isEqualTo(SUCCESS);

        // Check that the file exists and contains a selected sample of expected
        // lines:
        assertThat(new File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "package_sequence.puml"))
            .exists()
            .satisfies(file -> assertThat(PlantUmlDiagramTestUtils.lines(file))
                .contains(
                    "participant Flight",
                    "activate Flight",
                    "Flight -> Airport: load(example.Airplane)",
                    "activate Airport",
                    "Airport -> Airplane: load()",
                    "activate Airplane",
                    "deactivate Airplane",
                    "deactivate Airport",
                    "Flight -> Airplane: launch()",
                    "Flight -> Airplane: launch()\n" +
                    "activate Airplane",
                    "note RIGHT",
                    "Directly after launch, the",
                    "plane needs to **retract** its wheels.",
                    "end note"));
    }
}