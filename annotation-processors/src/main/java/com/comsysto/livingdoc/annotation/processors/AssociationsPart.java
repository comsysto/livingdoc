package com.comsysto.livingdoc.annotation.processors;

import lombok.Value;

import javax.lang.model.element.Name;

@Value
public class AssociationsPart {
    Name left;
    Name right;
    String generated;
}
