package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlDependency;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable;

@PlantUmlClass(diagramIds = { "airport", "!default" })
@PlantUmlDependency(target = FlyingVehicle.class, description = "restricted parking\\ncapabilities")
@PlantUmlDependency(target = Airplane.class)
public class Airport {

    @PlantUmlExecutable
    public void load(final Airplane airplane) {
        airplane.load();
    }
}
