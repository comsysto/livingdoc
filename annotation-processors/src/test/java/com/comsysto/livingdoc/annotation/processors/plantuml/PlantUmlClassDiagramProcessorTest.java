package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compilation.Status;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import javax.tools.JavaFileObject;

@SuppressWarnings("ConstantConditions")
public class PlantUmlClassDiagramProcessorTest {

    private File exampleDir;

    @Before
    public void setUp() throws Exception {
        final File projectRoot = Paths.get(this.getClass().getClassLoader().getResource(".").toURI())
            .getParent()
            .getParent()
            .getParent()
            .toFile();
        exampleDir = new File(projectRoot, "src/test/java/com/comsysto/livingdoc/example");
    }

    @Test
    @SneakyThrows
    public void process() {
        final Compilation result = Compiler.javac()
            .withProcessors(new PlantUmlClassDiagramProcessor())
            .compile(stream(exampleDir.listFiles()).map(this::javaFileObject).collect(toList()));
        assertThat(result.status()).isEqualTo(Status.SUCCESS);
    }

    @SneakyThrows
    private JavaFileObject javaFileObject(final File f) {
        return JavaFileObjects.forResource(f.toURI().toURL());
    }
}