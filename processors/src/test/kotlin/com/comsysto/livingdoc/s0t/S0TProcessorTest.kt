package com.comsysto.livingdoc.s0t

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType
import com.comsysto.livingdoc.s0t.model.*
import com.comsysto.livingdoc.s0t.model.TypeModel.Type
import com.comsysto.livingdoc.s0t.model.relations.Association
import com.comsysto.livingdoc.s0t.model.relations.Inheritance
import com.comsysto.livingdoc.s0t.render.OutputRenderer
import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.exist
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

private const val PACKAGE = "com.comsysto.livingdoc.s0t.example"

internal class S0TProcessorTest : BehaviorSpec({
    Given("the test directory") {
        val testDir = System.getProperty(KEY_TEST_DIR)
        val packagePath = "com/comsysto/livingdoc/s0t/example"
        val exampleDir = File(if (testDir != null) File(testDir) else testSourcesRoot(), packagePath)
        val sut = S0tProcessor()

        When("I run the annotation processor") {
            val files = exampleDir.listFiles()
            if (files == null || files.isEmpty()) throw IllegalStateException("$exampleDir contains no source files.")

            System.setProperty(
                KEY_RENDERERS,
                "com.comsysto.livingdoc.s0t.ModelCaptor,com.comsysto.livingdoc.s0t.render.FreemarkerRenderer"
            )
            val options = System.getProperties()
                .filter { with(it.key.toString()) { startsWith("s0t.") && !startsWith("s0t.test.") } }
                .map { "-A${it.key}=${it.value}" }

            val result = javac()
                .withProcessors(sut)
                .withOptions(options)
                .compile(files.map { javaFileObject(it) })
            val classDiagramFile =
                File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "${packagePath}/example-class.puml")
            val airportClassDiagramFile =
                File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "${packagePath}/airport-class.puml")
            val sequenceDiagramFile =
                File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "${packagePath}/example-sequence.puml")

            Then("the processing status should be SUCCESS") { result.status() shouldBe Compilation.Status.SUCCESS }

            // Check the models:
            And("the type model should contain the expected content") {
                assertThat(ModelCaptor.capturedModel.toString())
                    .isEqualTo(expectedModel().toString())
            }

            // Check the general class diagram:
            And("the general class diagram file exists") { classDiagramFile should exist() }
            And("the general class diagram contains the expected content") {
                nonEmptyLinesTrimmed(classDiagramFile) shouldContainExactly nonEmptyLinesTrimmed(
                    File(
                        testClassLoaderRoot(),
                        "/expected/expected-example-class.puml"
                    )
                )
            }

            // Check the ground vehicles class diagram:
            And("the airport class diagram file exists") { classDiagramFile should exist() }
            And("the airport class diagram contains the expected content") {
                nonEmptyLinesTrimmed(airportClassDiagramFile) shouldContainExactly nonEmptyLinesTrimmed(
                    File(
                        testClassLoaderRoot(),
                        "/expected/expected-airport-class.puml"
                    )
                )
            }

            // Check the generated sequence diagram:
            And("the sequence diagram file exists") { sequenceDiagramFile should exist() }
            And("the sequence diagram contains the expected content") {
                nonEmptyLinesTrimmed(sequenceDiagramFile) shouldContainExactly nonEmptyLinesTrimmed(
                    File(
                        testClassLoaderRoot(),
                        "/expected/expected-example-sequence.puml"
                    )
                )
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
            .filter { it.isNotEmpty() }
            .collect(Collectors.toList())
    }
}

private fun expectedModel(): S0tModel {
    val expected = S0tModel()
    val typeRefWing = complexTypeRef("Wing")
    val typeRefAirplane = complexTypeRef("Airplane")
    val typeRefFlyingVehicle = complexTypeRef("FlyingVehicle")
    val typeRefAirport = complexTypeRef("Airport")
    val fieldWings = FieldModel(
        "wings", TypeRef(
            TypeName.ComplexTypeName(PACKAGE, "Tuple"), TypeRef.Kind.COMPLEX
        ),
        AccessModifier.PROTECTED,
        listOf(
            typeRefWing,
            typeRefWing
        )
    )
    expected.addType(
        TypeModel(
            name = typeRefAirplane.name as TypeName.ComplexTypeName,
            type = Type.CLASS,
            fields = listOf(
                fieldWings,
            ),
            inheritance = Inheritance(typeRefFlyingVehicle, typeRefAirplane),
            associations = listOf(
                Association(
                    typeRefAirplane,
                    typeRefWing,
                    fieldWings,
                    "",
                    "2",
                    AssociationType.COMPOSITION
                )
            ),
            notes = listOf(
                NoteModel(
                    "This models an airplane, a\n//flying// \nvehicle that is\n**very** fast.",
                    Position.BOTTOM
                )
            ),
            diagramIds = setOf("default", "airport")
        ),
    )
    expected.addType(
        TypeModel(
            name = complexTypeName("Airport"),
            type = Type.CLASS,
            dependencies = listOf(typeRefAirport)
        )
    )
    expected.addType(TypeModel(complexTypeName("Car"), Type.CLASS, listOf()))
    expected.addType(TypeModel(complexTypeName("Flying"), Type.INTERFACE, listOf()))
    expected.addType(TypeModel(complexTypeName("FlyingVehicle"), Type.CLASS, listOf()))
    expected.addType(TypeModel(complexTypeName("GroundVehicle"), Type.ABSTRACT, listOf()))
    expected.addType(TypeModel(complexTypeName("Train"), Type.CLASS, listOf()))
    expected.addType(TypeModel(complexTypeName("TransportType"), Type.ENUM, listOf()))
    expected.addType(TypeModel(complexTypeName("Vehicle"), Type.CLASS, listOf()))
    expected.addType(TypeModel(complexTypeName("Wing"), Type.CLASS, listOf()))

    return expected
}

private fun complexTypeRef(simpleName: String) = TypeRef(complexTypeName(simpleName), TypeRef.Kind.COMPLEX)

private fun complexTypeName(simpleName: String) = TypeName.ComplexTypeName(PACKAGE, simpleName)

class ModelCaptor() : OutputRenderer {

    override fun render(model: S0tModel) {
        capturedModel = model
    }

    companion object {
        lateinit var capturedModel: S0tModel
    }
}
