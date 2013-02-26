package com.sap.sailing.domain.swisstimingadapter.classes.messages;

public class VERMessage {
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public VERMessage(String version) {
        super();
        this.version = version;
    }

    public VERMessage() {
        super();
    }
    
    public String toString(){
        return "VER|" + version;
    }

}
