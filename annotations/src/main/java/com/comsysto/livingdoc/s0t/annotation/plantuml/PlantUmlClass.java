package com.comsysto.livingdoc.s0t.annotation.plantuml;

import static com.comsysto.livingdoc.s0t.annotation.plantuml.AutoCreateType.DEFAULT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be attached to a type that should be rendered as part of one or
 * more <a href="http://plantuml.com/class-diagram">PlantUML class diagram</a>s.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE })
public @interface PlantUmlClass {

    String[] diagramIds() default { "package" };

    /**
     * A flag that tells the annotation processor if all fields should be added
     * to the class model automatically.
     *
     * @return <code>YES</code> if fields are to be added automatically or
     * <code>NO</code> if only fields with a {@link PlantUmlField} annotation
     * shall be added.
     */
    AutoCreateType autoCreateFields() default DEFAULT;

    /**
     * A flag that tells the annotation processor if field associations should
     * be added to the class model automatically.
     *
     * @return <code>YES</code> if field associations are to be added
     * automatically or <code>NO</code> if only fields with a
     * {@link PlantUmlField} annotation shall be added.
     */
    AutoCreateType autoCreateAssociations() default DEFAULT;
    
}
