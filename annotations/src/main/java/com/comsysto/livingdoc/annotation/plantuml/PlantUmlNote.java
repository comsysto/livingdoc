package com.comsysto.livingdoc.annotation.plantuml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({
            ElementType.PACKAGE,
            ElementType.TYPE,
            ElementType.ANNOTATION_TYPE,
            ElementType.CONSTRUCTOR,
            ElementType.METHOD,
            ElementType.FIELD
        })
@Repeatable(PlantUmlNotes.class)
public @interface PlantUmlNote {

    String body() default "";
    Position position() default Position.TOP;

    enum Position {
        TOP, BOTTOM, LEFT, RIGHT;
    }

}

