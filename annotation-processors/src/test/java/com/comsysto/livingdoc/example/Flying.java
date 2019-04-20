package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlMethod;

@PlantUmlClass
public interface Flying {

    @PlantUmlMethod
    void fly();
}
