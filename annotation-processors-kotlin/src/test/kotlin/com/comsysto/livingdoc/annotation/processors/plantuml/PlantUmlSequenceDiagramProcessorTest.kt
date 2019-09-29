package com.comsysto.livingdoc.annotation.processors.plantuml

import com.comsysto.livingdoc.kotlin.annotation.processors.DEF_OUT_DIR
import com.comsysto.livingdoc.kotlin.annotation.processors.KEY_OUT_DIR
import com.comsysto.livingdoc.kotlin.annotation.processors.PlantUmlSequenceDiagramProcessor
import com.google.testing.compile.Compilation.Status.SUCCESS
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import io.kotlintest.matchers.file.exist
import io.kotlintest.should
import io.kotlintest.shouldBe
import org.apache.log4j.BasicConfigurator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import javax.tools.JavaFileObject

class PlantUmlSequenceDiagramProcessorTest {
    private lateinit var exampleDir: File

    @Before
    fun setup() {
        val testDirectory = System.getProperty(TEST_DIR)
        exampleDir = File(
                if (testDirectory != null) File(testDirectory) else testClassLoaderRoot(),
                "com/comsysto/livingdoc/example")
    }

    @Test
    fun `Should run successfully and produce expected output`() {
        val result = Compiler.javac()
                .withProcessors(PlantUmlSequenceDiagramProcessor)
                .withOptions("-Apumlgen.out.dir=out")
                .compile(exampleDir.listFiles()?.map { this.javaFileObject(it) })

        result.status() shouldBe SUCCESS

        // Check that the file exists and contains a selected sample of expected
        // lines:
        val f = File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "package_sequence.puml")
        f should exist()

        assertThat(lines(f)).containsAll(listOf(
                "participant Flight",
                "activate Flight",
                "Flight -> Airport: load(com.comsysto.livingdoc.example.Airplane)",
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
                "end note"))

    }


    private fun lines(file: File): Set<String> {
        return Files.lines(file.toPath())
                .map { it -> it.trim { it <= ' ' } }
                .collect(Collectors.toSet())
    }


    private fun javaFileObject(f: File): JavaFileObject {
        return JavaFileObjects.forResource(f.toURI().toURL())
    }

    companion object {
        private const val TEST_DIR = "pumlgen.test.dir"

        @BeforeClass
        @JvmStatic
        fun setupBeforeClass() {
            BasicConfigurator.configure()
        }

        private fun testClassLoaderRoot() = File(projectRoot(), "src/test/java")

        private fun projectRoot() = Paths.get(this::class.java.classLoader.getResource(".")!!.toURI())
                .parent.parent.parent.toFile().absoluteFile
    }
}