package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;

@PlantUmlClass(diagramIds = "airport")
public interface Flying {

    void launch();

    void fly();

    void land();
}
