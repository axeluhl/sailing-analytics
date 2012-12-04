package com.sap.sailing.domain.swisstimingreplayadapter;

public enum BoatType {

    Competitor((byte) 1), Jury((byte) 2), Buoy((byte) 3), TimingScoring((byte) 4), Committee((byte) 5), Unknown((byte) 99);
    
    private final byte code;

    private BoatType(byte code) {
        this.code = code;
    }
    
    public static BoatType byCode(byte code) {
        if (code == 99) {
            return Unknown;
        } else {
            return values()[code - 1];
        }
        
    }
    
}
