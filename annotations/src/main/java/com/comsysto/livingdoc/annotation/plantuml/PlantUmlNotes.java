package com.comsysto.livingdoc.annotation.plantuml;

import java.lang.annotation.ElementType;
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
public @interface PlantUmlNotes {
    PlantUmlNote[] value() default {};
}
