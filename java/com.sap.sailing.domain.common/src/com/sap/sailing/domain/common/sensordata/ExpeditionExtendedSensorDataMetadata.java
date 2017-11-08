package com.sap.sailing.domain.common.sensordata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;

/**
 * Metadata that defines the column structure of BravoExtended fixes when imported as {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}.
 * 
 * The current implementation only stores a subset of the information available during the import.
 * 
 * The first columns need to be consistent with {@link BravoSensorDataMetadata}.
 */
public enum ExpeditionExtendedSensorDataMetadata {
    HEEL("Heel", BravoExtendedSensorDataMetadata.HEEL), //
    TRIM("TRIM", BravoExtendedSensorDataMetadata.PITCH), //
    LEEWAY(null, BravoExtendedSensorDataMetadata.LEEWAY), //
    SET(null, BravoExtendedSensorDataMetadata.SET), //
    DRIFT(null, BravoExtendedSensorDataMetadata.DRIFT), //
    DEPTH(null, BravoExtendedSensorDataMetadata.DEPTH), //
    RUDDER(null, BravoExtendedSensorDataMetadata.RUDDER), //
    FORESTAY_LOAD(null, BravoExtendedSensorDataMetadata.FORESTAY_LOAD), //
    TACK_ANGLE(null, BravoExtendedSensorDataMetadata.TACK_ANGLE), //
    RAKE_DEG(null, BravoExtendedSensorDataMetadata.RAKE_DEG), //
    DEFLECTOR_PERCENTAGE(null, BravoExtendedSensorDataMetadata.DEFLECTOR_PERCENTAGE), //
    TARGET_HEEL(null, BravoExtendedSensorDataMetadata.TARGET_HEEL), //
    DEFLECTOR_MILLIMETERS(null, BravoExtendedSensorDataMetadata.DEFLECTOR_MILLIMETERS), //
    TARGET_BOATSPEED_P(null, BravoExtendedSensorDataMetadata.TARGET_BOATSPEED_P), //

    ;

    private String columnName;
    
    /**
     * Expedition extended fixes are mapped to Bravo extended fixes whose metadata is described
     * in {@link BravoExtendedSensorDataMetadata}. When parsing an Expedition file, the columns
     * in the resulting {@link DoubleVectorFix} must be chosen such that they match up with the
     * corresponding fields as described by {@link BravoExtendedSensorDataMetadata}. This turns
     * the ordinals of this enum's literals insignificant; the position is taken solely from
     * the Bravo metadata.
     */
    private final BravoExtendedSensorDataMetadata mappedToBravoField;
    
    private ExpeditionExtendedSensorDataMetadata(String columnName, BravoExtendedSensorDataMetadata mappedToBravoField) {
        this.columnName = columnName;
        this.mappedToBravoField = mappedToBravoField;
    }
    
    public String getColumnName() {
        return columnName;
    }

    /**
     * The index in the {@link DoubleVectorFix} where this data item will be stored
     */
    public int getColumnIndex() {
        return mappedToBravoField.getColumnIndex();
    }

    public boolean isExpectedInLogFile() {
        return mappedToBravoField.isExpectedInLogFile();
    }

    public static ExpeditionExtendedSensorDataMetadata byColumnName(String valueName) {
        ExpeditionExtendedSensorDataMetadata[] values = ExpeditionExtendedSensorDataMetadata.values();
        for (ExpeditionExtendedSensorDataMetadata item : values) {
            if (Objects.equals(item.getColumnName(), valueName)) {
                return item;
            }
        }
        return null;
    }

    public static int getTrackColumnCount() {
        return ExpeditionExtendedSensorDataMetadata.values().length;
    }

    public static List<String> getTrackColumnNames() {
        ArrayList<String> colNames = new ArrayList<>(getTrackColumnCount());
        for (ExpeditionExtendedSensorDataMetadata item : ExpeditionExtendedSensorDataMetadata.values()) {
            colNames.add(item.getColumnName());
        }
        return colNames;
    }

    public static Map<String, Integer> getColumnNamesToIndexInDoubleFix() {
        final Map<String, Integer> columnNamesToIndexInDoubleFix = new HashMap<>();
        for (final ExpeditionExtendedSensorDataMetadata column : ExpeditionExtendedSensorDataMetadata.values()) {
            if (column.isExpectedInLogFile()) {
                columnNamesToIndexInDoubleFix.put(column.getColumnName(), column.getColumnIndex());
            }
        }
        return columnNamesToIndexInDoubleFix;
    }
}
