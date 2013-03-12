package com.sap.sailing.odf.resultimport;

public interface Person extends Named {
    public static enum Gender { M, F };
    
    Gender getGender();
}
