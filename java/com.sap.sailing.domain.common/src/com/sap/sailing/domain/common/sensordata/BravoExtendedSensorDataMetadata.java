package com.sap.sailing.domain.common.sensordata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Metadata that defines the column structure of BravoExtended fixes when imported as {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}.
 * 
 * The current implementation only stores a subset of the information available during the import.
 * 
 * The first columns need to be consistent with {@link BravoSensorDataMetadata}.
 */
public enum BravoExtendedSensorDataMetadata {
    RIDE_HEIGHT_PORT_HULL("RideHeightPort"), //
    RIDE_HEIGHT_STBD_HULL("RideHeightStbd"), //
    HEEL("Heel"), //
    PITCH("PitchRate"),
    DB_RAKE_PORT("DaggerBoardRakeAnglePort"), //
    DB_RAKE_STBD("DaggerBoardRakeAngleStbd"), //
    RUDDER_RAKE_PORT("RudderRakeAnglePort"), //
    RUDDER_RAKE_STBD("RudderRakeAngleStbd"), //
    MAST_ROTATION("MastRotation");

    private String columnName;
    
    private BravoExtendedSensorDataMetadata(String columnName) {
        this.columnName = columnName;
    }
    
    public String getColumnName() {
        return columnName;
    }

    public int getColumnIndex() {
        return this.ordinal();
    }

    public static final int HEADER_COLUMN_OFFSET = 3;

    public static BravoExtendedSensorDataMetadata byColumnName(String valueName) {
        BravoExtendedSensorDataMetadata[] values = BravoExtendedSensorDataMetadata.values();
        for (BravoExtendedSensorDataMetadata item : values) {
            if (Objects.equals(item.getColumnName(), valueName)) {
                return item;
            }
        }
        return null;
    }

    public static int getTrackColumnCount() {
        return BravoExtendedSensorDataMetadata.values().length;
    }

    public static List<String> getTrackColumnNames() {
        ArrayList<String> colNames = new ArrayList<>(getTrackColumnCount());
        for (BravoExtendedSensorDataMetadata item : BravoExtendedSensorDataMetadata.values()) {
            colNames.add(item.getColumnName());
        }
        return colNames;
    }

    public static Map<String, Integer> getColumnNamesToIndexInDoubleFix() {
        final Map<String, Integer> columnNamesToIndexInDoubleFix = new HashMap<>();
        for (final BravoExtendedSensorDataMetadata column : BravoExtendedSensorDataMetadata.values()) {
            columnNamesToIndexInDoubleFix.put(column.getColumnName(), column.getColumnIndex());
        }
        return columnNamesToIndexInDoubleFix;
    }
}
