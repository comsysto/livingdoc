package com.comsysto.livingdoc.annotation.plantuml;

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
}
