package com.sap.sailing.domain.common.sensordata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Metadata that defines the column structure of Bravo fixes when imported as
 * {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}.
 * <p>
 * 
 * The current implementation only stores a subset of the information available during the import.
 */
public enum BravoSensorDataMetadata implements ColumnMetadata {
    RIDE_HEIGHT_PORT_HULL("RideHeightPortHull"), //
    RIDE_HEIGHT_STBD_HULL("RideHeightStbdHull"), //
    HEEL("Heel"), //
    PITCH("ImuSensor_Pitch");

    private String columnName;
    
    private BravoSensorDataMetadata(String columnName) {
        this.columnName = columnName;
    }
    
    public String getColumnName() {
        return columnName;
    }

    public int getColumnIndex() {
        return this.ordinal();
    }

    public static final int HEADER_COLUMN_OFFSET = 3;

    public static BravoSensorDataMetadata byColumnName(String valueName) {
        BravoSensorDataMetadata[] values = BravoSensorDataMetadata.values();
        for (BravoSensorDataMetadata item : values) {
            if (Objects.equals(item.getColumnName(), valueName)) {
                return item;
            }
        }
        return null;
    }

    public static int getTrackColumnCount() {
        return BravoSensorDataMetadata.values().length;
    }

    public static List<String> getTrackColumnNames() {
        ArrayList<String> colNames = new ArrayList<>(getTrackColumnCount());
        for (BravoSensorDataMetadata item : BravoSensorDataMetadata.values()) {
            colNames.add(item.getColumnName());
        }
        return colNames;
    }

    public static Map<String, Integer> getColumnNamesToIndexInDoubleFix() {
        final Map<String, Integer> columnNamesToIndexInDoubleFix = new HashMap<>();
        for (final BravoSensorDataMetadata column : BravoSensorDataMetadata.values()) {
            columnNamesToIndexInDoubleFix.put(column.getColumnName(), column.getColumnIndex());
        }
        return columnNamesToIndexInDoubleFix;
    }
}
