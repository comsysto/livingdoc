package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;

@PlantUmlClass
public class FlyingVehicle extends Vehicle implements Flying {

    @Override
    public void fly() {
    }
}
