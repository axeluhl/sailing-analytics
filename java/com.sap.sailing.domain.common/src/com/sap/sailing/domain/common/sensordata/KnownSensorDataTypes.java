package com.sap.sailing.domain.common.sensordata;

public enum KnownSensorDataTypes {
    BRAVO(BravoSensorDataMetadata.INSTANCE);

    private SensorDataMetadata md;

    private KnownSensorDataTypes(SensorDataMetadata md) {
        this.md = md;
    }

    public SensorDataMetadata getSensorDataMetadata() {
        return md;
    }
}
