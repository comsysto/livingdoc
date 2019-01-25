package com.comsysto.livingdoc.annotation.processors.plantuml;

import lombok.NonNull;
import lombok.Value;

@Value
public class DiagramId {
    private static final DiagramId DEFAULT = new DiagramId("package");
    private final String value;

    private DiagramId(final String value) {
        this.value = value;
    }

    public static DiagramId of(@NonNull final String s) {
        return s.equals(DEFAULT.value) ? DEFAULT : new DiagramId(s);
    }
}
