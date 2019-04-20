package com.comsysto.livingdoc.annotation.processors.plantuml.model;

import lombok.Value;

import javax.lang.model.element.TypeElement;

@Value
public class AssociationPart {

    AssociationId id;
    TypeElement left;
    TypeElement right;
    Relation relation;

    public enum Relation {
        IMPLEMENTS, EXTENDS, REFERENCES
    }
}
