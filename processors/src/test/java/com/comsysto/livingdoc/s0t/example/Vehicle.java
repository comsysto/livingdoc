package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField;

@PlantUmlClass(diagramIds = "ground-vehicles")
public class Vehicle {

    @PlantUmlField
    public TransportType transportType;
}
