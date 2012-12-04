package com.sap.sailing.domain.swisstimingreplayadapter;

import java.util.HashMap;
import java.util.Map;

public enum MarkType {

    MANUAL((byte) 1), LINE_CROSSING((byte) 2), BUOY_ROUNDIN((byte) 3), UNKNOWN_167((byte) -89), UNKNOWN_171((byte) -85), UNKNOWN_179((byte) -77);
    
    private static final Map<Byte, MarkType> byCode;

    static {
        byCode = new HashMap<Byte, MarkType>();
        for (MarkType markType : MarkType.values()) {
            byCode.put(markType.code, markType);
        }
    }

    private final byte code;
    
    private MarkType(byte code) {
        this.code = code;
    }

    public static MarkType byCode(byte code) {
        MarkType result = byCode.get(code);
        if (result != null) {
            return result;
        } else {
            throw new IllegalArgumentException("Unknown mark type: " + code);
        }
    }

}
