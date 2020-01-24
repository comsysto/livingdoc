package com.comsysto.livingdoc.annotation.processors.plantuml;

import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.KEY_ENABLED;
import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.KEY_OUT_DIR;
import static com.comsysto.livingdoc.annotation.processors.plantuml.PlantUmlClassDiagramProcessor.KEY_SETTINGS_DIR;
import static com.google.common.collect.Sets.union;
import static java.util.Arrays.stream;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static javax.tools.Diagnostic.Kind.WARNING;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlDependency;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNotes;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.ClassDiagram;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.DiagramId;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.ExecutablePart;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.SequenceDiagram;
import com.comsysto.livingdoc.annotation.processors.plantuml.model.TypePart;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

/**
 * The main processor class that handles top-level annotations for UMl diagrams
 * (currently only {@link PlantUmlClass}).
 */
@SuppressWarnings("unused")
@SupportedAnnotationTypes({
                              "com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass",
                              "com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable.StartOfSequence"
                          })
@SupportedOptions({ KEY_SETTINGS_DIR, KEY_OUT_DIR, KEY_ENABLED })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@Slf4j
@PlantUmlClass(diagramIds = PlantUmlClassDiagramProcessor.DIAGRAM_ID)
@PlantUmlDependency(target = "ClassDiagram", description = "generates/processes")
public class PlantUmlClassDiagramProcessor extends AbstractProcessor {
    protected static final String KEY_SETTINGS_DIR = "pumlgen.settings.dir";
    protected static final String DEF_SETTINGS_DIR = ".";
    protected static final String KEY_OUT_DIR = "pumlgen.out.dir";
    protected static final String KEY_ENABLED = "pumlgen.enabled";
    protected static final String DEF_ENABLED = "true";
    protected static final String DEF_OUT_DIR = "./out";
    public static final String DIAGRAM_ID = "annotation-processor";
    private final Configuration freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_23);

    private String settingsDir;
    private String outDir;

    public PlantUmlClassDiagramProcessor() {
        freemarkerConfiguration.setTemplateLoader(new ClassTemplateLoader(this.getClass(), ""));
    }

    @Override
    @PlantUmlExecutable
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        log.debug("Starting processing of PlantUML annotations.");

        settingsDir = processingEnv.getOptions().getOrDefault(KEY_SETTINGS_DIR, DEF_SETTINGS_DIR);
        outDir = processingEnv.getOptions().getOrDefault(KEY_OUT_DIR, getSourcePath());

        if (!roundEnv.errorRaised() && !roundEnv.processingOver()) {
            annotations.forEach(annotation -> processAnnotation(annotation, roundEnv));
            return true;
        }
        return false;
    }

    /**
     * Process a specific annotation.
     *
     * @param annotation the annotation
     * @param roundEnv   the round environment
     */
    private void processAnnotation(final TypeElement annotation, final RoundEnvironment roundEnv) {
        if (isEnabled()) {
            switch (annotation.getQualifiedName().toString()) {
                case "com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass":
                    final Set<TypeElement> annotated = roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .map(TypeElement.class::cast)
                        .collect(toSet());

                    processPlantumlClassAnnotation(annotated);
                    break;

                case "com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable.StartOfSequence":
                    final Set<ExecutableElement> annotatedExecutables = roundEnv.getElementsAnnotatedWith(annotation)
                        .stream()
                        .map(ExecutableElement.class::cast)
                        .collect(toSet());

                    processPlantumlSequenceStartAnnotation(annotatedExecutables);
                    break;

                default:
                    log.error(
                        "Unexpected annotation type: {}. Most likely there is a mismatch between the value of "
                        + "@SupportedAnnotationTypes and the annotation handling implemented in "
                        + "PlantUmlClassDiagramProcessor.processAnnotation.",
                        annotation.getQualifiedName().toString());
            }
        } else {
            log.info("PlantUML class diagram processing is disabled.");
        }
    }

    /**
     * Defines an annotation processing option that can be used to disable the
     * processor (required to compile this very project).
     *
     * @return true if the processor is enabled.
     */
    private boolean isEnabled() {
        return toBoolean(processingEnv.getOptions().getOrDefault(KEY_ENABLED, DEF_ENABLED));
    }

    /**
     * Process the {@link PlantUmlClass} annotation for all types that have this
     * annotation. The processor will generate one diagram file per unique
     * diagram ID found on any annotation.
     *
     * @param annotatedTypes the annotated types.
     */
    private void processPlantumlClassAnnotation(final Set<TypeElement> annotatedTypes) {
        final Map<DiagramId, List<TypePart>> generatedFiles = annotatedTypes.stream()
            .map(this::createDiagramPart)
            .flatMap(part -> getDiagramIds(part).stream()
                .map(id -> new TypePart(
                    singleton(id),
                    part.getAnnotation(),
                    part.getTypeElement(),
                    part.getNotes())))
            .collect(groupingBy(part -> getDiagramIds(part).stream().findFirst().get()));

        generatedFiles.keySet().forEach(id -> createClassDiagram(id, generatedFiles.get(id)));
    }

    private Set<DiagramId> getDiagramIds(final TypePart part) {
        return union(part.getDiagramIds(), singleton(DiagramId.of("package")));
    }

    private void createClassDiagram(final DiagramId diagramId, final List<TypePart> parts) {
        final File outFile = getOutFile(diagramId, "class");
        final Properties settings = loadSettings(diagramId);

        log.debug("Create PlantUML diagram: {}", outFile.getAbsolutePath());

        try (final BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
            final Template template = freemarkerConfiguration.getTemplate("class-diagram.puml.ftl");
            template.process(
                new ClassDiagram(
                    settings.getProperty("title", null),
                    stream(settings.getProperty("include.files", "").split(","))
                        .filter(StringUtils::isNoneBlank)
                        .collect(toList()),
                    parts),
                out);
        } catch (IOException | TemplateException e) {
            log.error("Failed to generate diagram: " + outFile, e);
            throw new RuntimeException(e);
        }
    }

    private TypePart createDiagramPart(final TypeElement annotated) {
        final PlantUmlClass classAnnotation = annotated.getAnnotation(PlantUmlClass.class);
        log.debug("Processing PlantUmlClass annotation on type: {}", annotated);

        return new TypePart(
            stream(classAnnotation.diagramIds()).map(DiagramId::of).collect(toSet()),
            classAnnotation,
            annotated,
            getNoteAnnotations(annotated));
    }

    private void processPlantumlSequenceStartAnnotation(final Set<ExecutableElement> annotatedExecutables) {
        final Map<DiagramId, List<ExecutablePart>> generatedFiles = annotatedExecutables.stream()
            .map(this::createDiagramPart)
            .flatMap(part -> part.getDiagramIds().stream()
                .map(id -> new ExecutablePart(
                    processingEnv,
                    singleton(id),
                    part.getAnnotation(),
                    part.getAnnotated())))
            .collect(groupingBy(part -> part.getDiagramIds().stream().findFirst().get()));

        generatedFiles.keySet().forEach(id -> createSequenceDiagram(id, generatedFiles.get(id)));
    }

    @SneakyThrows
    private void createSequenceDiagram(final DiagramId diagramId, final List<ExecutablePart> parts) {
        final File outFile = getOutFile(diagramId, "sequence");
        final Properties settings = loadSettings(diagramId);

        log.debug("Create PlantUML diagram: {}", outFile.getAbsoluteFile());

        try (final BufferedWriter out = new BufferedWriter(new FileWriter(outFile))) {
            final Template template = freemarkerConfiguration.getTemplate("sequence-diagram.puml.ftl");
            template.process(
                new SequenceDiagram(
                    settings.getProperty("title", null),
                    stream(settings.getProperty("include.files", "").split(","))
                        .filter(StringUtils::isNoneBlank)
                        .collect(toList()),
                    parts.stream().sorted().collect(toList())),
                out);
        } catch (IOException | TemplateException e) {
            log.error("Failed to generate diagram: " + outFile, e);
            throw new RuntimeException(e);
        }
    }

    private ExecutablePart createDiagramPart(final ExecutableElement annotated) {
        final PlantUmlExecutable annotation = annotated.getAnnotation(PlantUmlExecutable.class);
        log.debug("Processing PlantUmlExecutable annotation on executable: {}", annotated);

        return new ExecutablePart(
            processingEnv,
            stream(annotation.diagramIds())
                .map(DiagramId::of)
                .collect(toSet()),
            annotation,
            annotated);
    }

    private List<PlantUmlNote> getNoteAnnotations(final TypeElement annotated) {
        return Optional.ofNullable(annotated.getAnnotation(PlantUmlNotes.class))
            .map(PlantUmlNotes::value)
            .map(Arrays::asList)
            .orElseGet(() -> Optional.ofNullable(annotated.getAnnotation(PlantUmlNote.class))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList()));
    }

    private File getOutFile(final DiagramId diagramId, final String type) {
        final File diagramFile = new File(outDir, diagramId.getValue() + "_" + type + ".puml");

        //noinspection ResultOfMethodCallIgnored
        diagramFile.getParentFile().mkdirs();
        return diagramFile;
    }

    private String getSourcePath() {
        try {
            final JavaFileObject generatedObject = processingEnv.getFiler()
                .createSourceFile("test_" + getClass().getSimpleName());
            final Writer writer = generatedObject.openWriter();
            final File sourcePath = new File(generatedObject.toUri().getPath()).getParentFile();

            writer.close();
            generatedObject.delete();
            return sourcePath.getAbsolutePath();
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(WARNING, "Unable to determine source file path!");
            return DEF_OUT_DIR;
        }
    }

    private Properties loadSettings(final DiagramId id) {
        final File settingsFile = new File(settingsDir, id.getValue() + "_class.properties");
        final Properties settings = new Properties();

        try (final FileReader in = new FileReader(settingsFile)) {
            log.debug("Settings file: {}", settingsFile.getAbsoluteFile());
            settings.load(in);
            log.debug("Settings: \n{}", settings.toString());
        } catch (IOException e) {
            log.debug("No settings file found: {}", settingsFile.getAbsoluteFile());
        }
        return settings;
    }
}