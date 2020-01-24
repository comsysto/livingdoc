package example;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlField;

@PlantUmlClass(diagramIds = { "package", "ground-vehicles" })
public class Train extends GroundVehicle {

    @PlantUmlField
    Car loadedCar;

    public Train(final Car loadedCar, int numberOfWheels) {
        this.loadedCar = loadedCar;
        this.setNumberOfWheels(numberOfWheels);
    }
}
