package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;

@PlantUmlClass
public class Train extends GroundVehicle {

    @PlantUmlField
    Car loadedCar;
}
