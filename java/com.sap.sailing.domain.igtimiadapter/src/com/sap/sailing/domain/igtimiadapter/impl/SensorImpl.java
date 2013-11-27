package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Sensor;

public class SensorImpl implements Sensor {
    private final String transmitterId;
    private final String deviceId;
    protected SensorImpl(String transmitterId, String deviceId) {
        super();
        this.transmitterId = transmitterId;
        this.deviceId = deviceId;
    }
    
    @Override
    public String getTransmitterId() {
        return transmitterId;
    }
    
    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
        result = prime * result + ((transmitterId == null) ? 0 : transmitterId.hashCode());
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
        SensorImpl other = (SensorImpl) obj;
        if (deviceId == null) {
            if (other.deviceId != null)
                return false;
        } else if (!deviceId.equals(other.deviceId))
            return false;
        if (transmitterId == null) {
            if (other.transmitterId != null)
                return false;
        } else if (!transmitterId.equals(other.transmitterId))
            return false;
        return true;
    }

}
