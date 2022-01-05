package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote;
import com.comsysto.livingdoc.s0t.annotation.plantuml.Position;

@PlantUmlClass(diagramIds = { "package", "ground-vehicles" })
@PlantUmlNote(body = "A vehicle that drives on the ground", position = Position.TOP)
@PlantUmlNote(body = "Multiple notes may be\nattached to a type", position = Position.RIGHT)
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
