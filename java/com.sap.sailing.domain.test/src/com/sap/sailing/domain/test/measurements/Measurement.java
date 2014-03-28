package com.sap.sailing.domain.test.measurements;

public class Measurement {
    private final String name;
    private final double value;

    public Measurement(String name, double value) {
        super();
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

}
