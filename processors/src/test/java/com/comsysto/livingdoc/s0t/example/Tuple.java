package com.comsysto.livingdoc.s0t.example;

/**
 * A tuple type, serving as example for a custom container type that won't
 * show up in diagrams as an association target.
 *
 * @param <T1>
 * @param <T2>
 */
public class Tuple<T1, T2> {
    T1 left;
    T2 right;
}
