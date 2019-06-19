package annotation.processors.plantuml

import org.apache.log4j.BasicConfigurator
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Arrays.stream
import java.util.stream.Collectors.toSet
import javax.tools.JavaFileObject

class PlantUmlClassDiagramProcessorTest {
    private var exampleDir: File? = null

    private val testClassLoaderRoot: File
        get() {
            val projectRoot = Paths.get(this.javaClass.getClassLoader().getResource(".")!!.toURI())
                    .getParent()
                    .getParent()
                    .getParent()
                    .toFile()
            return File(projectRoot, "src/test/java")
        }

    @Before
    fun setUp() {
        val testRoot = File(System.getProperty(TEST_DIR)) ?: getTestClassLoaderRoot()
        exampleDir = File(testRoot, "com/comsysto/livingdoc/example")
    }

    @Test
    fun should_run_successfully_and_produce_expected_output() {
        val result = Compiler.javac()
                .withProcessors(PlantUmlClassDiagramProcessor())
                .withOptions("-Apumlgen.out.dir=out")
                .compile(stream(exampleDir!!.listFiles()!!)
                        .map { this.javaFileObject(it) })

        assertThat(result.status()).isEqualTo(Status.SUCCESS)

        // Check that the file exists and contains a selected sample of expected
        // lines:
        assertThat(File(System.getProperty(KEY_OUT_DIR, DEF_OUT_DIR), "package_class.puml"))
                .exists()
                .satisfies({ file ->
                    assertThat<String>(lines(file))
                            .contains(
                                    "class Car",
                                    "Wing leftWing",
                                    "Flying <|.. FlyingVehicle",
                                    "FlyingVehicle <|-- Airplane",
                                    "Airplane --> Wing")
                })
    }


    private fun lines(file: File): Set<String> {
        return Files.lines(file.toPath())
                .map { it.trim({ it <= ' ' }) })
    }


    private fun javaFileObject(f: File): JavaFileObject {
        return JavaFileObjects.forResource(f.toURI().toURL())
    }

    companion object {

        protected val TEST_DIR = "pumlgen.test.dir"

        @BeforeClass
        fun setupBeforeClass() {
            BasicConfigurator.configure()
        }
    }
}