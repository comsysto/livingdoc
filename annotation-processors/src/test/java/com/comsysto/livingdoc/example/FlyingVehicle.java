package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;

@PlantUmlClass
public class FlyingVehicle extends Vehicle implements Flying {

    @PlantUmlExecutable
    @Override
    public void launch() {
        
    }

    @PlantUmlExecutable
    @Override
    public void fly() {
    }

    @PlantUmlExecutable
    @Override
    public void land() {

    }
}
