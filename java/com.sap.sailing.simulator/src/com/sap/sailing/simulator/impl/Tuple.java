package com.sap.sailing.simulator.impl;

public class Tuple<X, Y, Z> {
    public final int index;
    public final X first;
    public final Y second;
    public final Z third;

    public Tuple(X x, Y y, Z z) {
        this.index = 0;
        this.first = x;
        this.second = y;
        this.third = z;
    }

    public Tuple(X x, Y y, Z z, int index) {
        this.index = index;
        this.first = x;
        this.second = y;
        this.third = z;
    }
}
