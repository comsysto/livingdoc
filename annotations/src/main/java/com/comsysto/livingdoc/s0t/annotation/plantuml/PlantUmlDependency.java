package com.comsysto.livingdoc.s0t.annotation.plantuml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Repeatable(PlantUmlDependencies.class)
public @interface PlantUmlDependency {
    Class<?> target();
    String description() default "";
}
