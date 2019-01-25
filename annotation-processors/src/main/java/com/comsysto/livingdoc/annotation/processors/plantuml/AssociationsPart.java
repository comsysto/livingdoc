package com.comsysto.livingdoc.annotation.processors.plantuml;

import lombok.Value;

import javax.lang.model.element.TypeElement;

@Value
public class AssociationsPart {
    TypeElement left;
    TypeElement right;
}
