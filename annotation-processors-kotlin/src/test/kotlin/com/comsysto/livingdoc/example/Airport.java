package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;

public class Airport {

    @PlantUmlExecutable
    public void load(final Airplane airplane) {
        airplane.load();
    }
}
