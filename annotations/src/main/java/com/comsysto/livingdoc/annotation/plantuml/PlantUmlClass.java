package com.comsysto.livingdoc.annotation.plantuml;

import static com.comsysto.livingdoc.annotation.plantuml.AccessModifier.NONE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface PlantUmlClass {

    String[] diagramIds() default { "package" };
    AccessModifier autoIncludeNonPropertyFields() default NONE;
    AccessModifier autoIncludeNonPropertyMethods() default NONE;
    AccessModifier autoIncludeProperties() default NONE;
    AccessModifier autoIncludeVirtualProperties() default NONE;
}
