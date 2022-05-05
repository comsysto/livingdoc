package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote;

import java.util.List;

/**
 * Models a train. A train may carry a cargo of cars. This ridiculously
 * superfluous JavaDoc comment serves as a test for the note auto-generation
 * capability.
 */
@PlantUmlClass(diagramIds = "ground-vehicles")
@PlantUmlNote
public class Train extends GroundVehicle {

    @PlantUmlField(targetCardinality = "0..*")
    public List<Car> loadedCars;

    public Train(final List<Car> loadedCars, int numberOfWheels) {
        this.loadedCars = loadedCars;
        this.setNumberOfWheels(numberOfWheels);
    }
}
