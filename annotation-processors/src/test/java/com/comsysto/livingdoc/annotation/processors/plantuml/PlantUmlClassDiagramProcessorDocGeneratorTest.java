package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import lombok.SneakyThrows;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;
import javax.tools.JavaFileObject;

@SuppressWarnings("ConstantConditions")
public class PlantUmlClassDiagramProcessorDocGeneratorTest {
    private static final String SRC_DIR = "pumlgen.src.dir";
    private Optional<File> srcRoot;

    @BeforeClass
    public static void setupBeforeClass() {
        BasicConfigurator.configure();
    }

    @Before
    public void setUp() {
        srcRoot = Optional.ofNullable(System.getProperty(SRC_DIR))
            .map(File::new);
    }

    @Test
    @SneakyThrows
    public void should_run_successfully_and_produce_expected_output() {
        srcRoot.ifPresent(dir -> {

            //noinspection ResultOfMethodCallIgnored
            Compiler.javac()
                .withProcessors(new PlantUmlClassDiagramProcessor())
                .withOptions("-Apumlgen.out.dir=out")
                .compile(streamFiles(dir)
                             .filter(file -> file.getName().endsWith(".java"))
                             .map(this::javaFileObject)
                             .collect(toList()));
        });
    }

    private Stream<File> streamFiles(final File dir) {
        return stream(dir.listFiles()).flatMap(file -> file.isDirectory() ? streamFiles(file) : Stream.of(file));
    }

    @SneakyThrows
    private JavaFileObject javaFileObject(final File f) {
        return JavaFileObjects.forResource(f.toURI().toURL());
    }
}