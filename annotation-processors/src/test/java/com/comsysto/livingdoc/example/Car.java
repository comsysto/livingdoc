package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;

@PlantUmlClass
public class Car extends GroundVehicle {

    public Car() {
        this.setNumberOfWheels(4);
    }
}
