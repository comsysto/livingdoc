package com.comsysto.livingdoc.annotation.plantuml;

import com.comsysto.livingdoc.annotation.plantuml.PlantUmlNote.Container;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
@Repeatable(Container.class)
public @interface PlantUmlNote {

    String body() default "";
    Position position() default Position.TOP;

    enum Position {
        TOP, BOTTOM, LEFT, RIGHT;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.FIELD })
    @interface Container {
        PlantUmlNote[] value() default {};
    }
}

