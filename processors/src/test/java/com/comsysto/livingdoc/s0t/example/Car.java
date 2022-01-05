package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;

@PlantUmlClass(diagramIds = { "package", "ground-vehicles" })
public class Car extends GroundVehicle {

    public Car() {
        this.setNumberOfWheels(4);
    }
}
