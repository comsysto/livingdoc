package com.comsysto.livingdoc.s0t.annotation.plantuml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that models a note to be included in a class diagram. Multiple
 * notes may be attached to the same type.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Repeatable(PlantUmlNotes.class)
public @interface PlantUmlNote {

    /**
     * The note body. The body may be formatted using
     * <a href="http://plantuml.com/creole">Creole</a>, the markup language used
     * by PlantUML.
     */
    String value() default "";

    /**
     * The note's position in relation to the rendered type.
     */
    Position position() default Position.TOP;

    /**
     * Max line length of the note.
     *
     * @return the maximum number of characters in a line.
     */
    int maxLineLength() default 30;

    /**
     * A flag indicating if long words shall be wrapped if they exceed
     * maxLineLength.
     *
     * @return true if long words should be wrapped.
     */
    boolean wrapLongwords() default true;

    /**
     * A regular expression used to specify characters where line breaks may be
     * inserted.
     *
     * @return a regular expression.
     */
    String wrapOn() default "";
}

