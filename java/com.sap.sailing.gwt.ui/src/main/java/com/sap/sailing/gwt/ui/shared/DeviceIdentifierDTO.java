package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class DeviceIdentifierDTO implements Serializable {
    private static final long serialVersionUID = 1171014272760543228L;
    public String deviceType;
    public String deviceId;
    
    protected DeviceIdentifierDTO() {}
    
    public DeviceIdentifierDTO(String deviceType, String deviceId) {
        this.deviceType = deviceType;
        this.deviceId = deviceId;
    }
    
    public String toString() {
        return deviceType + " - " + deviceId;
    }
}
