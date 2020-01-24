package com.comsysto.livingdoc.annotation.processors.plantuml;

import static java.util.stream.Collectors.toSet;

import com.google.testing.compile.JavaFileObjects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import javax.tools.JavaFileObject;

@UtilityClass
public class PlantUmlDiagramTestUtils {
    protected static final String TEST_DIR = "pumlgen.test.dir";

    @SneakyThrows
    public static File getTestClassLoaderRoot(ClassLoader cl) {
        final File projectRoot = Paths.get(cl.getResource(".").toURI())
            .getParent()
            .getParent()
            .getParent()
            .toFile();
        return new File(projectRoot, "src/test/java");
    }

    @SneakyThrows
    public static Set<String> lines(final File file) {
        return Files.lines(file.toPath())
            .map(String::trim)
            .collect(toSet());
    }

    @SneakyThrows
    public static JavaFileObject javaFileObject(final File f) {
        return JavaFileObjects.forResource(f.toURI().toURL());
    }
}
