package com.comsysto.livingdoc.s0t.example;

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable.StartOfSequence;

@PlantUmlClass(diagramIds = {"airport", "!default"})
public class Flight {

    private Airport airport;

    private Airplane airplane;

    @PlantUmlExecutable
    @StartOfSequence
    public void execute() {
        airport.load(airplane);

        airplane.launch();
        airplane.fly();
        airplane.land();
    }
}
