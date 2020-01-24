package example;

import static com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Position.BOTTOM;
import static com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Position.RIGHT;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlExecutable;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;

@PlantUmlClass
@PlantUmlNote(
    body = "This models an airplane, a //flying//\nvehicle that is **very** fast.",
    position = BOTTOM)
public class Airplane extends FlyingVehicle {

    @PlantUmlField
    Wing leftWing;

    @PlantUmlField
    Wing rightWing;

    @Override
    @PlantUmlExecutable
    @PlantUmlNote(position = RIGHT, body = "Directly after launch, the\nplane needs to **retract** its wheels.")
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
