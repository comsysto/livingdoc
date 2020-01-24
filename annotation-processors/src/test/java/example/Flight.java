package example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable.StartOfSequence;

public class Flight {

    private Airport airport;

    private Airplane airplane;

    @PlantUmlExecutable
    @StartOfSequence
    public void execute() {
        airport.load(airplane);

        airplane.launch();
        airplane.fly();
        airplane.land();
    }
}
