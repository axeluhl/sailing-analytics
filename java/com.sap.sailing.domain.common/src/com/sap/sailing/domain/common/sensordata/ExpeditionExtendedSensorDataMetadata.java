package com.sap.sailing.domain.common.sensordata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sap.sailing.domain.common.tracking.DoubleVectorFix;

/**
 * Metadata that defines the column structure of BravoExtended fixes when imported as
 * {@link com.sap.sailing.domain.common.tracking.DoubleVectorFix}.
 * <p>
 * 
 * The current implementation only stores a subset of the information available during the import.
 * <p>
 * 
 * The first columns need to be consistent with {@link BravoSensorDataMetadata}.
 */
public enum ExpeditionExtendedSensorDataMetadata {
    HEEL("Heel", BravoExtendedSensorDataMetadata.HEEL), //
    TRIM("Trim", BravoExtendedSensorDataMetadata.PITCH), //
    LEEWAY("Leeway", BravoExtendedSensorDataMetadata.LEEWAY, /* expected in logfile */ true), //
    SET("Set", BravoExtendedSensorDataMetadata.SET, /* expected in logfile */ true), //
    DRIFT("Drift", BravoExtendedSensorDataMetadata.DRIFT, /* expected in logfile */ true), //
    DEPTH("Depth", BravoExtendedSensorDataMetadata.DEPTH, /* expected in logfile */ true), //
    RUDDER("Rudder", BravoExtendedSensorDataMetadata.RUDDER, /* expected in logfile */ true), //
    FORESTAY_LOAD("Forestay", BravoExtendedSensorDataMetadata.FORESTAY_LOAD, /* expected in logfile */ true), //
    FORESTAY_PRESSURE("ForestayPres", BravoExtendedSensorDataMetadata.FORESTAY_PRESSURE, /* expected in logfile */ true), //
    TACK_ANGLE(null, BravoExtendedSensorDataMetadata.TACK_ANGLE), //
    RAKE_DEG("RakeDeg", BravoExtendedSensorDataMetadata.RAKE_DEG, /* expected in logfile */ true), //
    DEFLECTOR_PERCENTAGE("DeflectorP", BravoExtendedSensorDataMetadata.DEFLECTOR_PERCENTAGE, /* expected in logfile */ true), //
    TARGET_HEEL("TG Heel1", BravoExtendedSensorDataMetadata.TARGET_HEEL, /* expected in logfile */ true), //
    DEFLECTOR_MILLIMETERS("DflectrMM", BravoExtendedSensorDataMetadata.DEFLECTOR_MILLIMETERS, /* expected in logfile */ true), //
    TARGET_BOATSPEED_P("BspTr", BravoExtendedSensorDataMetadata.TARGET_BOATSPEED_P, /* expected in logfile */ true), //
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

    private final boolean expectedInLogfile;
    
    private ExpeditionExtendedSensorDataMetadata(String columnName, BravoExtendedSensorDataMetadata mappedToBravoField) {
        this(columnName, mappedToBravoField, mappedToBravoField.isExpectedInLogFile());
    }
    
    private ExpeditionExtendedSensorDataMetadata(String columnName, BravoExtendedSensorDataMetadata mappedToBravoField, boolean expectedInLogfile) {
        this.columnName = columnName;
        this.mappedToBravoField = mappedToBravoField;
        this.expectedInLogfile = expectedInLogfile;
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
        return expectedInLogfile;
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
