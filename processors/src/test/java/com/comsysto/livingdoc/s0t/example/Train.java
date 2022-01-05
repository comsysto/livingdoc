package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField;

import java.util.List;

@PlantUmlClass(diagramIds = { "package", "ground-vehicles" })
public class Train extends GroundVehicle {

    @PlantUmlField(targetCardinality = "0..*")
    public List<Car> loadedCars;

    public Train(final List<Car> loadedCars, int numberOfWheels) {
        this.loadedCars = loadedCars;
        this.setNumberOfWheels(numberOfWheels);
    }
}
