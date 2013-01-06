package com.sap.sailing.domain.swisstimingreplayadapter;

public enum CompetitorStatus {

    Sailing,
    Finished,
    DNS,
    DNF,
    DNC,
    OCS,
    BFD,
    DGM,
    DNE,
    DPI,
    DSQ,
    RAF,
    RGD,
    SCP,
    ZFP,
    Unknown;
    
    public static CompetitorStatus byCode(byte code) {
        final CompetitorStatus result;
        if (code >= 0 && values().length >= code+1) {
            result = values()[code];
        } else {
            result = Unknown;
        }
        return result;
    }
    
}
