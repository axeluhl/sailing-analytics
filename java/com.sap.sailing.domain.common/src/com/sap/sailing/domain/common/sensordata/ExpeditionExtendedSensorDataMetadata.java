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
    LEEWAY("Leeway", BravoExtendedSensorDataMetadata.LEEWAY), //
    SET("Set", BravoExtendedSensorDataMetadata.SET), //
    DRIFT("Drift", BravoExtendedSensorDataMetadata.DRIFT), //
    DEPTH("Depth", BravoExtendedSensorDataMetadata.DEPTH), //
    RUDDER("Rudder", BravoExtendedSensorDataMetadata.RUDDER), //
    FORESTAY_LOAD("Forestay", BravoExtendedSensorDataMetadata.FORESTAY_LOAD), //
    FORESTAY_PRESSURE("ForestayPres", BravoExtendedSensorDataMetadata.FORESTAY_PRESSURE), //
    TACK_ANGLE("TackAngle", BravoExtendedSensorDataMetadata.TACK_ANGLE), //
    RAKE_DEG("RakeDeg", BravoExtendedSensorDataMetadata.RAKE_DEG), //
    DEFLECTOR_PERCENTAGE("DflectrPP", BravoExtendedSensorDataMetadata.DEFLECTOR_PERCENTAGE), //
    TARGET_HEEL("TG Heell", BravoExtendedSensorDataMetadata.TARGET_HEEL), //
    DEFLECTOR_MILLIMETERS("DflectrMM", BravoExtendedSensorDataMetadata.DEFLECTOR_MILLIMETERS), //
    TARGET_BOATSPEED_P("BspTr", BravoExtendedSensorDataMetadata.TARGET_BOATSPEED_P), //
    EXPEDITION_AWA("Awa", BravoExtendedSensorDataMetadata.EXPEDITION_AWA), //
    EXPEDITION_AWS("Aws", BravoExtendedSensorDataMetadata.EXPEDITION_AWS), //
    EXPEDITION_TWA("Twa", BravoExtendedSensorDataMetadata.EXPEDITION_TWA), //
    EXPEDITION_TWS("Tws", BravoExtendedSensorDataMetadata.EXPEDITION_TWS), //
    EXPEDITION_TWD("Twd", BravoExtendedSensorDataMetadata.EXPEDITION_TWD), //
    EXPEDITION_BSP("Bsp", BravoExtendedSensorDataMetadata.EXPEDITION_BSP), //
    EXPEDITION_SOG("Sog", BravoExtendedSensorDataMetadata.EXPEDITION_SOG), //
    EXPEDITION_COG("Cog", BravoExtendedSensorDataMetadata.EXPEDITION_COG), //
    EXPEDITION_RAKE("Rake", BravoExtendedSensorDataMetadata.EXPEDITION_RAKE), //
    EXPEDITION_HDG("Hdg", BravoExtendedSensorDataMetadata.EXPEDITION_HDG), //
    EXPEDITION_TMTOGUN("TmToGun", BravoExtendedSensorDataMetadata.EXPEDITION_TMTOGUN), //
    EXPEDITION_TMTOBURN("TmToBurn", BravoExtendedSensorDataMetadata.EXPEDITION_TMTOBURN), //
    EXPEDITION_BELOWLN("BelowLn", BravoExtendedSensorDataMetadata.EXPEDITION_BELOWLN), //
    EXPEDITION_RATE_OF_TURN("ROT", BravoExtendedSensorDataMetadata.EXPEDITION_RATE_OF_TURN), //
    EXPEDITION_BARO("Baro", BravoExtendedSensorDataMetadata.EXPEDITION_BARO), //
    EXPEDITION_LOAD_P("Load P", BravoExtendedSensorDataMetadata.EXPEDITION_LOAD_P), //
    EXPEDITION_LOAD_S("Load S", BravoExtendedSensorDataMetadata.EXPEDITION_LOAD_S), //
    EXPEDITION_JIB_CAR_PORT("JibCarPort", BravoExtendedSensorDataMetadata.EXPEDITION_JIB_CAR_PORT), //
    EXPEDITION_JIB_CAR_STBD("JibCarStbd", BravoExtendedSensorDataMetadata.EXPEDITION_JIB_CAR_STBD), //
    EXPEDITION_MAST_BUTT("MastButt", BravoExtendedSensorDataMetadata.EXPEDITION_MAST_BUTT), //
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
            columnNamesToIndexInDoubleFix.put(column.getColumnName(), column.getColumnIndex());
        }
        return columnNamesToIndexInDoubleFix;
    }
}
