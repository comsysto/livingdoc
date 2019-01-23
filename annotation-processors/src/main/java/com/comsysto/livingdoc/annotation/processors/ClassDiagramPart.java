package com.comsysto.livingdoc.annotation.processors;

import lombok.Value;

import java.util.List;
import javax.lang.model.element.Name;

@Value
public class ClassDiagramPart {
    private final DiagramId diagramId;
    private final Name className;
    private final String generatedClassBody;
    private final List<AssociationsPart> associations;
}
