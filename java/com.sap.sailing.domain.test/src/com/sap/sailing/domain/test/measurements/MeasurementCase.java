package com.sap.sailing.domain.test.measurements;

import java.util.HashSet;
import java.util.Set;

public class MeasurementCase {
    private final String name;
    private final Set<Measurement> measurements;

    public MeasurementCase(String name) {
        super();
        this.name = name;
        measurements = new HashSet<>();
    }
    
    public String getName() {
        return name;
    }

    public void addMeasurement(Measurement measurement) {
        measurements.add(measurement);
    }
    
    public Iterable<Measurement> getMeasurements() {
        return measurements;
    }
}
