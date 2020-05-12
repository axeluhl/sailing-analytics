package com.sap.sailing.domain.igtimiadapter.datatypes;

import java.util.HashMap;
import java.util.Map;

/**
 * See also <a href="https://www.igtimi.com/api/v1/data_types">https://www.igtimi.com/api/v1/data_types</a> for a list of data types
 * published by the Igtimi API.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum Type {
    gps_latlong(1, GpsLatLong.class),
    gps_quality_indicator(2, GpsQualityIndicator.class),
    gps_quality_sat_count(3, GpsQualitySatCount.class),
    gps_quality_hdop(4, GpsQualityHdop.class),
    gps_altitude(5, GpsAltitude.class),
    COG(6, COG.class),
    HDGM(7, HDGM.class),
    HDG(8, HDG.class),
    SOG(9, SOG.class),
    STW(10, STW.class),
    AWA(11, AWA.class),
    AWS(12, AWS.class),
    ant_hrm(13, AntHrm.class),
    ant_cbst(14, AntCbst.class),
    File(16, File.class);

    public static Type valueOf(int code) {
        return typeByCode.get(code);
    }

    public int getCode() {
        return code;
    }
    
    public Class<? extends Fix> getFixClass() {
        return fixClass;
    }

    private final int code;
    private final Class<? extends Fix> fixClass;
    private final static Map<Integer, Type> typeByCode = new HashMap<>();
    private final static Map<Class<? extends Fix>, Type> typeByClass = new HashMap<>();

    private Type(int code, Class<? extends Fix> fixClass) {
        this.code = code;
        this.fixClass = fixClass;
    }
    
    public static Type getType(Class<? extends Fix> fixClass) {
        return typeByClass.get(fixClass);
    }
    
    static {
        for (Type type : Type.values()) {
            typeByCode.put(type.getCode(), type);
            typeByClass.put(type.getFixClass(), type);
        }
    }
}
