import static com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Position.BOTTOM;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlClass;
import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote;

@PlantUmlClass
@PlantUmlNote(
    body = "This models an airplane, a //flying//\nvehicle that is **very** fast.",
    position = BOTTOM)
public class Airplane extends FlyingVehicle {
}
