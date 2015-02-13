package com.sap.sailing.simulator;

public enum PathType {
    ONE_TURNER_LEFT("4#1-Turner Left"),
    ONE_TURNER_RIGHT("5#1-Turner Right"),
    OPPORTUNIST_LEFT("2#Opportunist Left"),
    OPPORTUNIST_RIGHT("3#Opportunist Right"),
    OMNISCIENT("1#Omniscient");
    
    private String txtId;

    PathType(String txtId) {
        this.txtId = txtId;
    }

    public String getTxtId() {
        return txtId;
    }

}
