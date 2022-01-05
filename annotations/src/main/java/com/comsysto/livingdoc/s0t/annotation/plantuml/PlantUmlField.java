package com.comsysto.livingdoc.s0t.annotation.plantuml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used on a field within a class annotated with
 * {@link PlantUmlClass}. This annotation causes the field to be included in
 * the class body and/or as an association to the corresponding type.
 */
@Retention(RetentionPolicy.SOURCE)
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

    /**
     * Only relevant for a parametrized target type. In this case, this flag may
     * be set to <i>true</i> to force the association to be handled as a standard
     * association instead of a container association.
     *
     * @return <i>true</i> if the association should be a standard association.
     */
    boolean forceStandardTypeAssociation() default false;

    /**
     * This parameter may be used to define the association's source
     * cardinality.
     *
     * @return the cardinality of the source side.
     */
    String sourceCardinality() default "";

    /**
     * This parameter may be used to define the association's target
     * cardinality.
     *
     * @return the cardinality of the target side.
     */
    String targetCardinality() default "";

    AssociationType associationType() default AssociationType.STANDARD;

    enum AssociationType {
        STANDARD, AGGREGATION, COMPOSITION
    }
}
