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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result + ((deviceType == null) ? 0 : deviceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeviceIdentifierDTO other = (DeviceIdentifierDTO) obj;
        if (deviceId == null) {
            if (other.deviceId != null)
                return false;
        } else if (!deviceId.equals(other.deviceId))
            return false;
        if (deviceType == null) {
            if (other.deviceType != null)
                return false;
        } else if (!deviceType.equals(other.deviceType))
            return false;
        return true;
    }

    public String toString() {
        return deviceType + " - " + deviceId;
    }
}
