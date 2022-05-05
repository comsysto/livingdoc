package com.comsysto.livingdoc.s0t.example;


import static com.comsysto.livingdoc.s0t.annotation.plantuml.Position.BOTTOM;
import static com.comsysto.livingdoc.s0t.annotation.plantuml.Position.RIGHT;

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField.AssociationType;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote;

@PlantUmlClass
@PlantUmlNote(
    value = "This models an airplane, a //flying// \nvehicle that is **very** fast.",
    position = BOTTOM)
public class Airplane extends FlyingVehicle {

    @PlantUmlField(associationType = AssociationType.COMPOSITION, targetCardinality = "2")
    protected Tuple<Wing, Wing> wings;

    @Override
    @PlantUmlExecutable
    @PlantUmlNote(position = RIGHT, value = "Directly after launch, the\nplane needs to **retract** its wheels.")
    public void launch() {
        super.launch();
        retractWheels();
    }

    @PlantUmlExecutable
    private void retractWheels() {
    }

    @Override
    @PlantUmlExecutable
    public void land() {
        lowerWheels();
        super.land();
    }

    @PlantUmlExecutable
    private void lowerWheels() {
    }

    @PlantUmlExecutable
    public void load() {
    }
}
