package com.comsysto.livingdoc.s0t.example;

import com.comsysto.livingdoc.s0t.annotation.plantuml.AutoCreateType;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote;

@PlantUmlClass(diagramIds = { "default", "ground-vehicles" }, autoCreateFields = AutoCreateType.NO)
@PlantUmlNote(value = "Indicates whether a vehicle transports passengers or cargo.")
public enum TransportType {
    @PlantUmlField PASSENGERS("Persons are passengers."),
    @PlantUmlField CARGO("Bags are cargo.");

    final String description;

    TransportType(final String description) {
        this.description = description;
    }
}
