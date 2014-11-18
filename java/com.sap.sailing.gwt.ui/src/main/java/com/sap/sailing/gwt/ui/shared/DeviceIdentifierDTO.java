package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DeviceIdentifierDTO implements IsSerializable {
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
