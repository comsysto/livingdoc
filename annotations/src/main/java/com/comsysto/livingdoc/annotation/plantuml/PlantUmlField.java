package com.comsysto.livingdoc.annotation.plantuml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used on a field within a class annotated with
 * {@link PlantUmlClass}. This annotation causes the field to be included in
 * the class body and/or as an association to the corresponding type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface PlantUmlField {

    /**
     * Indicates if the field should be rendered as part of the class body.
     */
    boolean showField() default true;

    /**
     * Indicates if an association to the corresponding type element should be
     * rendered in the diagram. Please note that this is only possible for types
     * that are themselves part of the diagram.
     */
    boolean showAssociation() default true;
}
