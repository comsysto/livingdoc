package com.comsysto.livingdoc.annotation.processors;

import com.google.auto.service.AutoService;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("com.comsysto.livingdoc.annotation.plantuml.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class PlantUmlClassProcessor extends AbstractProcessor {

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final File f = new File("test.puml");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
            out.write("@startuml\n"
                      + "class Car\n"
                      + "\n"
                      + "Driver - Car : drives >\n"
                      + "Car *- Wheel : have 4 >\n"
                      + "Car -- Person : < owns\n"
                      + "\n"
                      + "@enduml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}