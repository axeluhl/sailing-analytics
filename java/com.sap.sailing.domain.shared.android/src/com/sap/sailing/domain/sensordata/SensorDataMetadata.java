package com.sap.sailing.domain.sensordata;

import java.util.List;

public interface SensorDataMetadata {
    List<String> getColumns();
    boolean hasColumn(String columnName);
    int getColumnIndex(String columnName);
}
