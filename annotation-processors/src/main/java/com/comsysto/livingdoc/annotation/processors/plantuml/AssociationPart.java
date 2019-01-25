package com.comsysto.livingdoc.annotation.processors.plantuml;

import lombok.Value;

import javax.lang.model.element.TypeElement;

@Value
public class AssociationPart {
    TypeElement left;
    TypeElement right;
    Relation relation;

    public enum Relation {
        IMPLEMENTS, EXTENDS
    }
}
