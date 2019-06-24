package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;

@PlantUmlClass
public interface Flying {

    void launch();

    void fly();

    void land();
}
