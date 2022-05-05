package com.comsysto.livingdoc.s0t

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.exist
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

internal class S0TProcessorTest : BehaviorSpec({
    Given("the test directory") {
        val testDir = System.getProperty(KEY_TEST_DIR)
        val packagePath = "com/comsysto/livingdoc/s0t/example"
        val exampleDir = File(if (testDir != null) File(testDir) else testSourcesRoot(), packagePath)
        val sut = S0tProcessor()

        When("I run the annotation processor") {
            val files = exampleDir.listFiles()
            if (files == null || files.isEmpty()) throw IllegalStateException("$exampleDir contains no source files.")

            val options = System.getProperties()
                    .filter { with(it.key.toString()) { startsWith("s0t.") && !startsWith("s0t.test.") } }
                    .map { "-A${it.key}=${it.value}" }

            val result = javac()
                    .withProcessors(sut)
                    .withOptions(options)
                    .compile(files.map { javaFileObject(it) })
            val classDiagramFile = File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "${packagePath}/example-class.puml")
            val groundVehiclesClassDiagramFile = File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "${packagePath}/ground-vehicles-class.puml")
            val sequenceDiagramFile = File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "${packagePath}/example-sequence.puml")

            Then("the processing status should be SUCCESS") { result.status() shouldBe Compilation.Status.SUCCESS }

            // Check the general class diagram:
            And("the general class diagram file exists") { classDiagramFile should exist() }
            And("the general class diagram contains the expected content") {
                nonEmptyLinesTrimmed(classDiagramFile) shouldContainExactly nonEmptyLinesTrimmed(File(testClassLoaderRoot(), "/expected/expected-example-class.puml"))
            }

            // Check the ground vehicles class diagram:
            And("the  ground vehicles class diagram file exists") { classDiagramFile should exist() }
            And("the  ground vehicles class diagram contains the expected content") {
                nonEmptyLinesTrimmed(groundVehiclesClassDiagramFile) shouldContainExactly nonEmptyLinesTrimmed(File(testClassLoaderRoot(), "/expected/expected-ground-vehicles-class.puml"))
            }

            // Check the generated sequence diagram:
            And("the sequence diagram file exists") { sequenceDiagramFile should exist() }
            And("the sequence diagram contains the expected content") {
                nonEmptyLinesTrimmed(sequenceDiagramFile) shouldContainExactly nonEmptyLinesTrimmed(File(testClassLoaderRoot(), "/expected/expected-example-sequence.puml"))
            }
        }
    }
}) {
    companion object {
        private const val KEY_TEST_DIR = "s0t.test.dir"

        private fun testSourcesRoot() = File(testClassLoaderRoot().parentFile.parentFile, "src/test/java")
        private fun testClassLoaderRoot() = Paths.get(this::class.java.classLoader.getResource(".")!!.toURI()).toFile()

        private fun javaFileObject(f: File) = JavaFileObjects.forResource(f.toURI().toURL())

        private fun nonEmptyLinesTrimmed(file: File) = Files.lines(file.toPath())
            .map { it -> it.trim { it == ' ' } }
            .filter { it.isNotEmpty()}
            .collect(Collectors.toList())
    }
}
