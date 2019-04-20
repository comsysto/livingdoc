package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;

@PlantUmlClass
public abstract class GroundVehicle extends Vehicle {

    @PlantUmlField
    private int numberOfWheels;

    public int getNumberOfWheels() {
        return numberOfWheels;
    }

    public void setNumberOfWheels(final int numberOfWheels) {
        this.numberOfWheels = numberOfWheels;
    }
}
