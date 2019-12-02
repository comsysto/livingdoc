package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Position;

@PlantUmlClass(diagramIds = { "package", "ground-vehicles" })
@PlantUmlNote(body = "A vehicle that drives on the ground", position = Position.TOP)
@PlantUmlNote(body = "Multiple notes may be attached to a type", position = Position.RIGHT)
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
