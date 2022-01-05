package com.comsysto.livingdoc.s0t.example;

import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlNote;

@PlantUmlClass
@PlantUmlNote("Indicates whether a vehicle transports passengers or cargo.")
public enum TransportType {
    PASSENGERS("Persons are passengers."),
    CARGO("Bags are cargo.");

    final String description;

    TransportType(final String description) {
        this.description = description;
    }
}
