package com.sap.sailing.simulator;

public enum BoatDirection {
    NONE("None"),
    BEAT_LEFT("Beat Left"),
    BEAT_RIGHT("Beat Right"),
    REACH_LEFT("Reach Left"),
    REACH_RIGHT("Reach Right"),
    JIBE_LEFT("Jibe Left"),
    JIBE_RIGHT("Jibe Right");
    
    private String txtId;

    BoatDirection(String txtId) {
        this.txtId = txtId;
    }

    public String getTxtId() {
        return txtId;
    }

}
