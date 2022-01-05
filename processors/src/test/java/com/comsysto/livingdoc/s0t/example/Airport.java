package com.comsysto.livingdoc.s0t.example;


import com.comsysto.livingdoc.s0t.annotation.plantuml.PlantUmlExecutable;

public class Airport {

    @PlantUmlExecutable
    public void load(final Airplane airplane) {
        airplane.load();
    }
}
