package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;

@PlantUmlClass(diagramIds = { "package", "ground-vehicles" })
public class Car extends GroundVehicle {

    public Car() {
        this.setNumberOfWheels(4);
    }
}
