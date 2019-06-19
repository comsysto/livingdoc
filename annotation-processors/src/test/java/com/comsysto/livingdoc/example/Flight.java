package com.comsysto.livingdoc.example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;

public class Flight {

    private Airport airport;

    private Airplane airplane;

    @PlantUmlExecutable
    public void execute() {
        airport.load(airplane);

        airplane.launch();
        airplane.fly();
        airplane.land();
    }
}
