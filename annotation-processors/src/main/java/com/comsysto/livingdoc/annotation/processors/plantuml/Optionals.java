package com.comsysto.livingdoc.annotation.processors.plantuml;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Helper methods for working with {@link Optional}.
 */
@UtilityClass
public class Optionals {

    public static <T> Stream<T> stream(Optional<T> opt) {
        return opt.map(Stream::of)
            .orElse(Stream.empty());
    }
}
