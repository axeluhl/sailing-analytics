package com.sap.sailing.simulator;

public enum PointOfSail {
    TACKING("Tacking"),
    JIBING("Jibing"),
    REACHING("Reaching");
    
    private String txtId;

    PointOfSail(String txtId) {
        this.txtId = txtId;
    }

    public String getTxtId() {
        return txtId;
    }

}
