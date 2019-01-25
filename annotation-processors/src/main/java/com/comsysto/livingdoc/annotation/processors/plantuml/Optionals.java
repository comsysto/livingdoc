package com.comsysto.livingdoc.annotation.processors.plantuml;

import java.util.Optional;
import java.util.stream.Stream;

public class Optionals {

    public static <T> Stream<T> stream(Optional<T> opt) {
        return opt.map(Stream::of)
            .orElse(Stream.empty());
    }
}
