package com.sap.sailing.domain.swisstimingreplayadapter;

public enum Status {

    ZERO,
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
    
    public static Status byCode(byte code) {
        return values()[code];
    }
    
}
