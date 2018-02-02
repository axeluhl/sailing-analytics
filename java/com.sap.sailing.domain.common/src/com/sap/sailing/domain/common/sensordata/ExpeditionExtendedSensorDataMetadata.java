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
    DEFLECTOR_PERCENTAGE("DflectrPP", BravoExtendedSensorDataMetadata.DEFLECTOR_PERCENTAGE, /* expected in logfile */ true), //
    TARGET_HEEL("TG Heell", BravoExtendedSensorDataMetadata.TARGET_HEEL, /* expected in logfile */ true), //
    DEFLECTOR_MILLIMETERS("DflectrMM", BravoExtendedSensorDataMetadata.DEFLECTOR_MILLIMETERS, /* expected in logfile */ true), //
    TARGET_BOATSPEED_P("BspTr", BravoExtendedSensorDataMetadata.TARGET_BOATSPEED_P, /* expected in logfile */ true), //
    EXPEDITION_AWA("Awa", BravoExtendedSensorDataMetadata.EXPEDITION_AWA, /* expected in logfile */ true), //
    EXPEDITION_AWS("Aws", BravoExtendedSensorDataMetadata.EXPEDITION_AWS, /* expected in logfile */ true), //
    EXPEDITION_TWA("Twa", BravoExtendedSensorDataMetadata.EXPEDITION_TWA, /* expected in logfile */ true), //
    EXPEDITION_TWS("Tws", BravoExtendedSensorDataMetadata.EXPEDITION_TWS, /* expected in logfile */ true), //
    EXPEDITION_TWD("Twd", BravoExtendedSensorDataMetadata.EXPEDITION_TWD, /* expected in logfile */ true), //
    EXPEDITION_BSP("Bsp", BravoExtendedSensorDataMetadata.EXPEDITION_BSP, /* expected in logfile */ true), //
    EXPEDITION_BSP_TR("BspTr", BravoExtendedSensorDataMetadata.EXPEDITION_BSP_TR, /* expected in logfile */ true), //
    EXPEDITION_SOG("Sog", BravoExtendedSensorDataMetadata.EXPEDITION_SOG, /* expected in logfile */ true), //
    EXPEDITION_COG("Cog", BravoExtendedSensorDataMetadata.EXPEDITION_COG, /* expected in logfile */ true), //
    EXPEDITION_FORESTAY("Forestay", BravoExtendedSensorDataMetadata.EXPEDITION_FORESTAY, /* expected in logfile */ true), //
    EXPEDITION_RAKE("Rake", BravoExtendedSensorDataMetadata.EXPEDITION_RAKE, /* expected in logfile */ true), //
    EXPEDITION_HDG("Hdg", BravoExtendedSensorDataMetadata.EXPEDITION_HDG, /* expected in logfile */ true), //
    EXPEDITION_HEEL("Heel", BravoExtendedSensorDataMetadata.EXPEDITION_HEEL, /* expected in logfile */ true), //
    EXPEDITION_TGHEEL("TG Heell", BravoExtendedSensorDataMetadata.EXPEDITION_TGHEEL, /* expected in logfile */ true), //
    EXPEDITION_TMTOGUN("TmToGun", BravoExtendedSensorDataMetadata.EXPEDITION_TMTOGUN, /* expected in logfile */ true), //
    EXPEDITION_TMTOBURN("TmToBurn", BravoExtendedSensorDataMetadata.EXPEDITION_TMTOBURN, /* expected in logfile */ true), //
    EXPEDITION_BELOWLN("BelowLn", BravoExtendedSensorDataMetadata.EXPEDITION_TMTOBURN, /* expected in logfile */ true), //
    EXPEDITION_RATE_OF_TURN("ROT", BravoExtendedSensorDataMetadata.EXPEDITION_TMTOBURN, /* expected in logfile */ true), //
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
