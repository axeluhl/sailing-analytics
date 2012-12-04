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
    ZFP;
    
    public static CompetitorStatus byCode(byte code) {
        return values()[code];
    }
    
}
