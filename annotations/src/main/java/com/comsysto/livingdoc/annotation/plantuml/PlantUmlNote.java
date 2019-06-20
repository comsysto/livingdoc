package com.comsysto.livingdoc.annotation.plantuml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that models a note to be included in a class diagram. Multiple
 * notes may be attached to the same type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Repeatable(PlantUmlNotes.class)
public @interface PlantUmlNote {

    /**
     * The note body. The body may be formatted using
     * <a href="http://plantuml.com/creole">Creole</a>, the markup language used
     * by PlantUML.
     */
    String body() default "";

    /**
     * The note's position in relation to the rendered type.
     */
    Position position() default Position.TOP;

    enum Position {
        TOP, BOTTOM, LEFT, RIGHT;
    }

}

