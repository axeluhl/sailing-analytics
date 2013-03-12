package com.sap.sailing.odf.resultimport;

public interface Athlete extends Named {
    public static enum Gender { M, F };
    
    Gender getGender();
}
