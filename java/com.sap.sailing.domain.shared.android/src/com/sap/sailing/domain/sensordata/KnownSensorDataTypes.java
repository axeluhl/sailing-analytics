package com.sap.sailing.domain.sensordata;

public enum KnownSensorDataTypes {
    BRAVO(new BravoSensorDataMetadata());

    private SensorDataMetadata md;

    private KnownSensorDataTypes(SensorDataMetadata md) {
        this.md = md;
    }

    public SensorDataMetadata getSensorDataMetadata() {
        return md;
    }
}
