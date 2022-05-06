package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;

@PlantUmlClass(diagramIds = "airport")
public class FlyingVehicle extends Vehicle implements Flying {

    @Override
    public void launch() {
    }

    @Override
    public void fly() {
    }

    @Override
    public void land() {

    }
}
